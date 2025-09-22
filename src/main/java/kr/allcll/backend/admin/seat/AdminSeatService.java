package kr.allcll.backend.admin.seat;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;
import kr.allcll.backend.admin.seat.dto.ChangeSubjectsResponse;
import kr.allcll.backend.admin.seat.dto.SeatStatusResponse;
import kr.allcll.crawler.client.SeatClient;
import kr.allcll.crawler.client.model.SeatResponse;
import kr.allcll.crawler.client.payload.SeatPayload;
import kr.allcll.crawler.common.exception.CrawlerAllcllException;
import kr.allcll.crawler.common.properties.SjptProperties;
import kr.allcll.crawler.common.schedule.CrawlerScheduledTaskHandler;
import kr.allcll.crawler.credential.Credential;
import kr.allcll.crawler.credential.Credentials;
import kr.allcll.crawler.seat.CrawlerSeat;
import kr.allcll.crawler.subject.CrawlerSubject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminSeatService {

    private final AtomicLong lastSuccessCrawlingTime = new AtomicLong(0);
    private static final long RECENT_CRAWLING_SUCCESS_THRESHOLD_MS = 3_000;

    private final SeatClient seatClient;
    private final Credentials credentials;
    private final AllSeatBuffer allSeatBuffer;
    private final ChangeDetector changeDetector;
    private final SjptProperties sjptProperties;
    private final TargetSubjectStorage targetSubjectStorage;
    private final CrawlerScheduledTaskHandler seatScheduler;
    private final SeatPersistenceService seatPersistenceService;
    private final SeatStreamStatusService seatStreamStatusService;

    public void getAllSeatPeriodically(String userId) {
        seatStreamStatusService.updateStatus(SeatStreamStatus.LIVE);
        Credential credential = credentials.findByUserId(userId);
        fetchPinSeat(credential);
        fetchGeneralSeat(credential);
    }

    public void getSeasonSeatPeriodically(String userId) {
        seatStreamStatusService.updateStatus(SeatStreamStatus.LIVE);
        Credential credential = credentials.findByUserId(userId);
        fetchPinSeat(credential);
        fetchGeneralSeat(credential);
    }

    public void cancelSeatScheduling() {
        seatScheduler.cancelAll();
        seatStreamStatusService.updateStatus(SeatStreamStatus.IDLE);
    }

    public SeatStatusResponse getSeatCrawlerStatus() {
        int seatSchedulerTaskCount = seatScheduler.getTaskCount();
        boolean validSeatSchedulerCount = seatSchedulerTaskCount == sjptProperties.getRequestPerSecondCount();
        boolean validRecentCrawlingSuccess =
            (System.currentTimeMillis() - lastSuccessCrawlingTime.get()) <= RECENT_CRAWLING_SUCCESS_THRESHOLD_MS;

        boolean isActive = validSeatSchedulerCount && validRecentCrawlingSuccess;

        return SeatStatusResponse.of(isActive);
    }

    private void fetchPinSeat(Credential credential) {
        int pinSubjectRequestPerSecondCount = sjptProperties.getPinSubjectRequestPerSecondCount();
        for (int i = 0; i < pinSubjectRequestPerSecondCount; i++) {
            seatScheduler.scheduleAtFixedRate(
                () -> sendPinSubjectRequest(credential),
                Duration.ofSeconds(1)
            );
        }
    }

    private void fetchGeneralSeat(Credential credential) {
        int requestPerSecondCount = sjptProperties.getRequestPerSecondCount();
        int pinSubjectRequestPerSecondCount = sjptProperties.getPinSubjectRequestPerSecondCount();
        for (int i = 0; i < requestPerSecondCount - pinSubjectRequestPerSecondCount; i++) {
            seatScheduler.scheduleAtFixedRate(
                () -> sendGeneralSubjectRequest(credential),
                Duration.ofSeconds(1)
            );
        }
    }

    private void sendPinSubjectRequest(Credential credential) {
        CrawlerSubject crawlerSubject = targetSubjectStorage.getNextPinTarget();
        if (crawlerSubject == null) {
            return;
        }
        sendExternalRequestWithOutDetect(crawlerSubject, credential);
    }

    private void sendGeneralSubjectRequest(Credential credential) {
        CrawlerSubject crawlerSubject = targetSubjectStorage.getNextGeneralTarget();
        sendExternalRequestWithOutDetect(crawlerSubject, credential);
    }

    private void sendExternalRequestWithOutDetect(CrawlerSubject crawlerSubject, Credential credential) {
        try {
            log.info("[SeatService] [학교 서버] 요청 시도 과목: {}", crawlerSubject);
            SeatPayload requestPayload = SeatPayload.from(crawlerSubject);
            SeatResponse response = seatClient.execute(credential, requestPayload);
            CrawlerSeat renewedCrawlerSeat = createSeat(response, crawlerSubject);

            allSeatBuffer.add(
                ChangeSubjectsResponse.of(
                    crawlerSubject.getId(),
                    ChangeStatus.UPDATE, //의미없는 필드...
                    SeatUtils.getRemainSeat(renewedCrawlerSeat),
                    LocalDateTime.now()
                    //renewedCrawlerSeat.getCreatedAt() //추후 개션 예정
                )
            );

            synchronized (getSubjectLock(renewedCrawlerSeat.getId())) {
                seatPersistenceService.saveSeat(renewedCrawlerSeat);
            }

            lastSuccessCrawlingTime.updateAndGet(
                previousSuccessTime -> Math.max(previousSuccessTime, System.currentTimeMillis())
            );

        } catch (CrawlerAllcllException e) {
            log.error(
                "[여석] 외부 API 호출에 실패했습니다. 과목: " + crawlerSubject.getCuriNo() + "-" + crawlerSubject.getClassName());
            seatStreamStatusService.updateStatus(SeatStreamStatus.ERROR);
        }
    }

    /**
     * 변경감지로 정책 변경 시 해당 메서드로 변경
     */
    private void sendExternalRequest(CrawlerSubject crawlerSubject, Credential credential) {
        log.info("[SeatService] [학교 서버] 요청 시도 과목: {}", crawlerSubject);
        SeatPayload requestPayload = SeatPayload.from(crawlerSubject);
        SeatResponse response = seatClient.execute(credential, requestPayload);
        CrawlerSeat renewedCrawlerSeat = createSeat(response, crawlerSubject);
        detectDifferenceAndSave(crawlerSubject, renewedCrawlerSeat);
    }

    private void detectDifferenceAndSave(CrawlerSubject crawlerSubject, CrawlerSeat renewedCrawlerSeat) {
        if (changeDetector.isRemainSeatChanged(crawlerSubject, renewedCrawlerSeat)) {
            changeDetector.saveChangeToBuffer(crawlerSubject, renewedCrawlerSeat);
            synchronized (getSubjectLock(crawlerSubject.getId())) {
                seatPersistenceService.saveSeat(renewedCrawlerSeat);
            }
        }
    }

    private CrawlerSeat createSeat(SeatResponse response, CrawlerSubject crawlerSubject) {
        return response.toSeat(crawlerSubject, LocalDate.now());
    }

    private Object getSubjectLock(Long subjectId) {
        return ("LOCK_" + subjectId).intern();
    }
}

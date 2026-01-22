package kr.allcll.backend.admin.seat;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import kr.allcll.backend.admin.seat.dto.SeatStatusResponse;
import kr.allcll.backend.admin.seat.dto.CrawledSubjectRemainingSeat;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import kr.allcll.backend.support.web.PrefixParser;
import kr.allcll.backend.support.web.TokenProvider;
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

    private static final long RECENT_CRAWLING_SUCCESS_THRESHOLD_MS = 3_000;
    private final SeatClient seatClient;
    private final Credentials credentials;
    private final TargetSubjectStorage targetSubjectStorage;
    private final CrawlerScheduledTaskHandler seatScheduler;
    private final SjptProperties sjptProperties;
    private final AllSeatBuffer allSeatBuffer;
    private final AtomicLong lastSuccessCrawlingTime = new AtomicLong(0);
    private final BatchService batchService;

    public void getAllSeatPeriodically(String userId) {
        Credential credential = credentials.findByUserId(userId);
        fetchPinSeat(credential);
        fetchGeneralSeat(credential);
    }

    public void getSeasonSeatPeriodically(String userId) {
        Credential credential = credentials.findByUserId(userId);
        fetchPinSeat(credential);
        fetchGeneralSeat(credential);
    }

    public void cancelSeatScheduling() {
        seatScheduler.cancelAll();
    }

    public SeatStatusResponse getSeatCrawlerStatus() {
        List<String> allTaskId = seatScheduler.getAllTaskId();
        List<String> userIds = PrefixParser.extractDistinct(allTaskId);
        if (userIds.isEmpty()) {
            return SeatStatusResponse.of(null, false);
        }
        if (userIds.size() > 1) {
            throw new AllcllException(AllcllErrorCode.SEAT_CRAWLING_IN_MULTIPLE_ACCOUNTS);
        }
        boolean isActive = isSeatCrawlingActive();
        return SeatStatusResponse.of(userIds.getFirst(), isActive);
    }

    private void fetchPinSeat(Credential credential) {
        int pinSubjectRequestPerSecondCount = sjptProperties.getPinSubjectRequestPerSecondCount();
        String prefixId = credential.makeUserIdPrefix();
        for (int i = 0; i < pinSubjectRequestPerSecondCount; i++) {
            seatScheduler.scheduleAtFixedRate(
                prefixId + TokenProvider.create(),
                () -> sendPinSubjectRequest(credential),
                Duration.ofSeconds(1)
            );
        }
    }

    private void fetchGeneralSeat(Credential credential) {
        int requestPerSecondCount = sjptProperties.getRequestPerSecondCount();
        int pinSubjectRequestPerSecondCount = sjptProperties.getPinSubjectRequestPerSecondCount();
        String prefixId = credential.makeUserIdPrefix();
        for (int i = 0; i < requestPerSecondCount - pinSubjectRequestPerSecondCount; i++) {
            seatScheduler.scheduleAtFixedRate(
                prefixId + TokenProvider.create(),
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
        crawlPinSeatAndBuffer(crawlerSubject, credential);
    }

    private void sendGeneralSubjectRequest(Credential credential) {
        CrawlerSubject crawlerSubject = targetSubjectStorage.getNextGeneralTarget();
        crawlGeneralSeatAndBuffer(crawlerSubject, credential);
    }

    private void crawlPinSeatAndBuffer(CrawlerSubject pinSubject, Credential credential) {
        try {
            CrawlerSeat crawlerSeat = sendExternalSeatRequest(pinSubject, credential);
            batchService.savePinSeatBatch(crawlerSeat);

            lastSuccessCrawlingTime.updateAndGet(
                previousSuccessTime -> Math.max(previousSuccessTime, System.currentTimeMillis())
            );
        } catch (CrawlerAllcllException e) {
            log.error(
                "[핀 과목 여석] 외부 API 호출에 실패했습니다. 과목: "
                    + pinSubject.getCuriNo() + "-"
                    + pinSubject.getClassName());
        }
    }

    private void crawlGeneralSeatAndBuffer(CrawlerSubject generalSubject, Credential credential) {
        try {
            CrawlerSeat crawlerSeat = sendExternalSeatRequest(generalSubject, credential);
            batchService.saveGeneralSeatBatch(crawlerSeat);

            lastSuccessCrawlingTime.updateAndGet(
                previousSuccessTime -> Math.max(previousSuccessTime, System.currentTimeMillis())
            );
        } catch (CrawlerAllcllException e) {
            log.error(
                "[교양 과목 여석] 외부 API 호출에 실패했습니다. 과목: "
                    + generalSubject.getCuriNo() + "-"
                    + generalSubject.getClassName());
        }
    }

    private CrawlerSeat sendExternalSeatRequest(CrawlerSubject crawlerSubject, Credential credential) {
        log.info("[AdminSeatService] [학교 서버] 요청 시도 과목: {}", crawlerSubject);
        SeatPayload requestPayload = SeatPayload.from(crawlerSubject);
        SeatResponse response = seatClient.execute(credential, requestPayload);
        CrawlerSeat renewedCrawlerSeat = createSeat(response, crawlerSubject);

        allSeatBuffer.add(
            CrawledSubjectRemainingSeat.of(
                crawlerSubject.getId(),
                SeatUtils.getRemainSeat(renewedCrawlerSeat),
                LocalDateTime.now()
            )
        );
        return renewedCrawlerSeat;
    }

    private boolean isSeatCrawlingActive() {
        boolean validSeatSchedulerCount = false;
        if (seatScheduler.getTaskCount() == sjptProperties.getRequestPerSecondCount()) {
            validSeatSchedulerCount = true;
        }

        boolean validRecentCrawlingSuccess = false;
        if (System.currentTimeMillis() - lastSuccessCrawlingTime.get() <= RECENT_CRAWLING_SUCCESS_THRESHOLD_MS) {
            validRecentCrawlingSuccess = true;
        }

        return validSeatSchedulerCount && validRecentCrawlingSuccess;
    }

    private CrawlerSeat createSeat(SeatResponse response, CrawlerSubject crawlerSubject) {
        return response.toSeat(crawlerSubject, LocalDate.now());
    }
}

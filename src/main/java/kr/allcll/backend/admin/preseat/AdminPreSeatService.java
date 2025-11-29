package kr.allcll.backend.admin.preseat;

import java.time.LocalDate;
import java.util.List;
import kr.allcll.backend.admin.preseat.dto.PreSeatResponse;
import kr.allcll.backend.admin.seat.SeatStreamStatus;
import kr.allcll.backend.admin.seat.SeatStreamStatusService;
import kr.allcll.crawler.client.SeatClient;
import kr.allcll.crawler.client.model.SeatResponse;
import kr.allcll.crawler.client.payload.SeatPayload;
import kr.allcll.crawler.common.entity.CrawlerSemester;
import kr.allcll.crawler.common.exception.CrawlerAllcllException;
import kr.allcll.crawler.credential.Credential;
import kr.allcll.crawler.credential.Credentials;
import kr.allcll.crawler.seat.CrawlerSeat;
import kr.allcll.crawler.subject.CrawlerSubject;
import kr.allcll.crawler.subject.CrawlerSubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPreSeatService {

    private final SeatClient seatClient;
    private final Credentials credentials;
    private final AllPreSeatBuffer allPreSeatBuffer;
    private final SeatStreamStatusService seatStreamStatusService;
    private final CrawlerSubjectRepository crawlerSubjectRepository;

    public void getAllPreSeat(String userId) {
        seatStreamStatusService.updateSeatStreamStatus(SeatStreamStatus.PRESEAT);
        Credential credential = credentials.findByUserId(userId);
        List<CrawlerSubject> crawlerSubjects = crawlerSubjectRepository.findAllBySemesterAt(CrawlerSemester.now());
        for (CrawlerSubject crawlerSubject : crawlerSubjects) {
            sendExternalPreSeatsRequest(crawlerSubject, credential);
        }
    }

    private void sendExternalPreSeatsRequest(CrawlerSubject crawlerSubject, Credential credential) {
        try {
            log.info("[SeatService] [학교 서버] 요청 시도 PreSeat: {}", crawlerSubject);
            SeatPayload requestPayload = SeatPayload.from(crawlerSubject);
            SeatResponse response = seatClient.execute(credential, requestPayload);
            CrawlerSeat renewedCrawlerSeat = createSeat(response, crawlerSubject);

            allPreSeatBuffer.add(
                PreSeatResponse.of(
                    crawlerSubject.getId(),
                    PreSeatUtils.getRemainPreSeat(renewedCrawlerSeat)
                )
            );
        } catch (CrawlerAllcllException e) {
            log.error(
                "[여석] 외부 API 호출에 실패했습니다. PreSeat: " + crawlerSubject.getCuriNo() + "-" + crawlerSubject.getClassName());
            seatStreamStatusService.updateSeatStreamStatus(SeatStreamStatus.ERROR);
        }
    }

    private CrawlerSeat createSeat(SeatResponse response, CrawlerSubject crawlerSubject) {
        return response.toSeat(crawlerSubject, LocalDate.now());
    }

}

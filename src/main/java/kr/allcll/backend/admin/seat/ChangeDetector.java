package kr.allcll.backend.admin.seat;

import java.util.List;
import java.util.Optional;
import kr.allcll.backend.admin.seat.dto.ChangeSubjectsResponse;
import kr.allcll.crawler.common.properties.SjptProperties;
import kr.allcll.crawler.seat.CrawlerSeat;
import kr.allcll.crawler.seat.CrawlerSeatRepository;
import kr.allcll.crawler.subject.CrawlerSubject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChangeDetector {

    private final CrawlerSeatRepository crawlerSeatRepository;
    private final LiveBoards liveBoards;
    private final ChangedSubjectBuffer changedSubjectBuffer;
    private final SjptProperties sjptProperties;

    public boolean isRemainSeatChanged(CrawlerSubject crawlerSubject, CrawlerSeat renewedCrawlerSeat) {
        Optional<CrawlerSeat> previousSeat = crawlerSeatRepository.findByCrawlerSubject(crawlerSubject);
        if (previousSeat.isEmpty()) {
            return true;
        }
        Integer previousRemainSeat = SeatUtils.getRemainSeat(previousSeat.get());
        Integer nowRemainSeat = SeatUtils.getRemainSeat(renewedCrawlerSeat);
        return !previousRemainSeat.equals(nowRemainSeat);
    }

    public void saveChangeToBuffer(CrawlerSubject crawlerSubject, CrawlerSeat renewedCrawlerSeat) {
        if (isGeneralSubject(crawlerSubject)) {
            List<ChangeSubjectsResponse> response = liveBoards.checkStatus(
                crawlerSubject,
                SeatUtils.getRemainSeat(renewedCrawlerSeat)
            );
            if (!response.isEmpty()) {
                changedSubjectBuffer.addAll(response);
            }
        } else {
            changedSubjectBuffer.add(
                ChangeSubjectsResponse.of(crawlerSubject.getId(), ChangeStatus.UPDATE, SeatUtils.getRemainSeat(
                    renewedCrawlerSeat)));
        }
    }

    private boolean isGeneralSubject(CrawlerSubject crawlerSubject) {
        return sjptProperties.getGeneralDeptCd().equals(crawlerSubject.getDeptCd());
    }
}

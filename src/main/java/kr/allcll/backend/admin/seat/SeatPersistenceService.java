package kr.allcll.backend.admin.seat;

import java.time.LocalDate;
import java.util.Optional;
import kr.allcll.crawler.seat.CrawlerSeat;
import kr.allcll.crawler.seat.CrawlerSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SeatPersistenceService {

    private final CrawlerSeatRepository crawlerSeatRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSeat(CrawlerSeat crawlerSeat) {
        LocalDate today = LocalDate.now();

        Optional<CrawlerSeat> existingSeatOpt = crawlerSeatRepository.findByCrawlerSubjectAndCreatedDate(
            crawlerSeat.getCrawlerSubject(),
            today
        );
        if (existingSeatOpt.isPresent()) {
            CrawlerSeat existingCrawlerSeat = existingSeatOpt.get();
            existingCrawlerSeat.merge(crawlerSeat);
        } else {
            crawlerSeatRepository.save(crawlerSeat);
        }
    }
}

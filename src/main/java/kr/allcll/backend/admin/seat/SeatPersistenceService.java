package kr.allcll.backend.admin.seat;

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
        crawlerSeatRepository.save(crawlerSeat);
    }
}

package kr.allcll.backend.admin.seat;

import java.util.List;
import kr.allcll.backend.support.batch.AbstractBatch;
import kr.allcll.crawler.seat.CrawlerSeat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeneralSeatBatch extends AbstractBatch<CrawlerSeat> {

    private static final int FLUSH_LIMIT = 15;

    private final SeatPersistenceService seatPersistenceService;

    @Override
    protected int flushLimit() {
        return FLUSH_LIMIT;
    }

    @Override
    protected void saveAll(List<CrawlerSeat> batch) {
        seatPersistenceService.saveAllSeat(batch);
    }
}

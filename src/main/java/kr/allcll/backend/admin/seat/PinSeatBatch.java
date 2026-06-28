package kr.allcll.backend.admin.seat;

import java.util.List;
import kr.allcll.backend.support.batch.AbstractBatch;
import kr.allcll.backend.support.metrics.SeatPipelineMetrics;
import kr.allcll.crawler.seat.CrawlerSeat;
import org.springframework.stereotype.Component;

@Component
public class PinSeatBatch extends AbstractBatch<CrawlerSeat> {

    private static final int FLUSH_LIMIT = 10;

    private final SeatPersistenceService seatPersistenceService;

    public PinSeatBatch(
        SeatPersistenceService seatPersistenceService,
        SeatPipelineMetrics seatPipelineMetrics
    ) {
        super(seatPipelineMetrics, "pin");
        this.seatPersistenceService = seatPersistenceService;
    }

    @Override
    protected int getFlushLimit() {
        return FLUSH_LIMIT;
    }

    @Override
    protected void saveAll(List<CrawlerSeat> batch) {
        seatPersistenceService.saveAllSeat(batch);
    }
}

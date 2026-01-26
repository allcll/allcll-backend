package kr.allcll.backend.admin.seat;

import kr.allcll.backend.support.scheduler.ScheduledTaskHandler;
import kr.allcll.crawler.seat.CrawlerSeat;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class BatchService {

    private final PinSeatBatch pinSeatBatch;
    private final GeneralSeatBatch generalSeatBatch;
    private final ScheduledTaskHandler scheduledTaskHandler;

    public BatchService(
        PinSeatBatch pinSeatBatch,
        GeneralSeatBatch generalSeatBatch,
        @Qualifier("seatBatchHandler") ScheduledTaskHandler scheduledTaskHandler
    ) {
        this.pinSeatBatch = pinSeatBatch;
        this.generalSeatBatch = generalSeatBatch;
        this.scheduledTaskHandler = scheduledTaskHandler;
    }

    public void savePinSeatBatch(CrawlerSeat renewedCrawlerSeat) {
        pinSeatBatch.savePinSeatToBatch(renewedCrawlerSeat);
    }

    public void saveGeneralSeatBatch(CrawlerSeat renewedCrawlerSeat) {
        generalSeatBatch.saveGeneralSeatToBatch(renewedCrawlerSeat);
    }

    public void cancelFlushScheduling() {
        scheduledTaskHandler.cancelAll();
    }
}

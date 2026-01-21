package kr.allcll.backend.admin.seat;

import java.time.Duration;
import java.util.List;
import kr.allcll.backend.support.scheduler.ScheduledTaskHandler;
import kr.allcll.crawler.seat.CrawlerSeat;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class BatchService {

    private static final Duration PIN_SEAT_SAVE_PERIOD = Duration.ofSeconds(1);
    private static final Duration GENERAL_SEAT_SAVE_PERIOD = Duration.ofSeconds(3);

    private final PinSeatBatch pinSeatBatch;
    private final GeneralSeatBatch generalSeatBatch;
    private final SeatPersistenceService seatPersistenceService;
    private final ScheduledTaskHandler scheduledTaskHandler;

    public BatchService(
        PinSeatBatch pinSeatBatch,
        GeneralSeatBatch generalSeatBatch,
        SeatPersistenceService seatPersistenceService,
        @Qualifier("seatBatchHandler") ScheduledTaskHandler scheduledTaskHandler
    ) {
        this.pinSeatBatch = pinSeatBatch;
        this.generalSeatBatch = generalSeatBatch;
        this.seatPersistenceService = seatPersistenceService;
        this.scheduledTaskHandler = scheduledTaskHandler;
    }

    public void savePinSeatBatch(CrawlerSeat renewedCrawlerSeat) {
        pinSeatBatch.savePinSeatToBatch(renewedCrawlerSeat);
    }

    public void saveGeneralSeatBatch(CrawlerSeat renewedCrawlerSeat) {
        generalSeatBatch.saveGeneralSeatToBatch(renewedCrawlerSeat);
    }

    public void flushAllSeatPeriodically() {
        flushPinSeatPeriodically();
        flushGeneralSeatPeriodically();
    }

    private void flushPinSeatPeriodically() {
        scheduledTaskHandler.scheduleAtFixedRate(
            flushPinSubject(),
            PIN_SEAT_SAVE_PERIOD
        );
    }

    private void flushGeneralSeatPeriodically() {
        scheduledTaskHandler.scheduleAtFixedRate(
            flushGeneralSubject(),
            GENERAL_SEAT_SAVE_PERIOD
        );
    }

    private Runnable flushPinSubject() {
        return () -> {
            List<CrawlerSeat> allCrawlerSeat = pinSeatBatch.getAll();
            seatPersistenceService.saveAllSeat(allCrawlerSeat);
        };
    }

    private Runnable flushGeneralSubject() {
        return () -> {
            List<CrawlerSeat> allCrawlerSeat = generalSeatBatch.getAll();
            seatPersistenceService.saveAllSeat(allCrawlerSeat);
        };
    }

    public void cancelFlushScheduling() {
        scheduledTaskHandler.cancelAll();
    }
}

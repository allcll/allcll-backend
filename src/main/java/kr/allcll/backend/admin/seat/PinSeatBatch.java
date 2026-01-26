package kr.allcll.backend.admin.seat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import kr.allcll.crawler.seat.CrawlerSeat;
import org.springframework.stereotype.Component;

@Component
public class PinSeatBatch {

    private static final int FLUSH_LIMIT = 10;

    private final BlockingQueue<CrawlerSeat> pinSeatBatch;
    private final SeatPersistenceService seatPersistenceService;
    private final Object lock;

    public PinSeatBatch(SeatPersistenceService seatPersistenceService) {
        this.seatPersistenceService = seatPersistenceService;
        this.pinSeatBatch = new LinkedBlockingQueue<>();
        this.lock = new Object();
    }

    public void savePinSeatToBatch(CrawlerSeat crawlerSeat) {
        pinSeatBatch.add(crawlerSeat);
        if (pinSeatBatch.size() >= FLUSH_LIMIT) {
            saveAll();
        }
    }

    public List<CrawlerSeat> getAll() {
        List<CrawlerSeat> flushResult = new ArrayList<>();
        pinSeatBatch.drainTo(flushResult);
        return flushResult;
    }

    private void saveAll() {
        List<CrawlerSeat> crawlerSeats;
        synchronized (lock) {
            crawlerSeats = getAll();
            if (crawlerSeats.isEmpty()) {
                return;
            }
        }
        seatPersistenceService.saveAllSeat(crawlerSeats);
    }
}

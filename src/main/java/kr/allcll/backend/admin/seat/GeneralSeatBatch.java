package kr.allcll.backend.admin.seat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import kr.allcll.crawler.seat.CrawlerSeat;
import org.springframework.stereotype.Component;

@Component
public class GeneralSeatBatch {

    private static final int FLUSH_LIMIT = 15;

    private final BlockingQueue<CrawlerSeat> generalSeatBatch;
    private final SeatPersistenceService seatPersistenceService;
    private final Object lock;

    public GeneralSeatBatch(SeatPersistenceService seatPersistenceService) {
        this.generalSeatBatch = new LinkedBlockingQueue<>();
        this.seatPersistenceService = seatPersistenceService;
        this.lock = new Object();
    }

    public void saveGeneralSeatToBatch(CrawlerSeat crawlerSeat) {
        generalSeatBatch.add(crawlerSeat);
        if (generalSeatBatch.size() >= FLUSH_LIMIT) {
            saveAll();
        }
    }

    public List<CrawlerSeat> getAll() {
        List<CrawlerSeat> flushResult = new ArrayList<>();
        generalSeatBatch.drainTo(flushResult);
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

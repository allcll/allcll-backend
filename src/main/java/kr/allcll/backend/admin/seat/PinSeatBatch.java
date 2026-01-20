package kr.allcll.backend.admin.seat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import kr.allcll.crawler.seat.CrawlerSeat;
import org.springframework.stereotype.Component;

@Component
public class PinSeatBatch {

    private final BlockingQueue<CrawlerSeat> pinSeatBatch;

    public PinSeatBatch() {
        this.pinSeatBatch = new LinkedBlockingQueue<>();
    }

    public void savePinSeatToBatch(CrawlerSeat crawlerSeat) {
        pinSeatBatch.add(crawlerSeat);
    }

    public List<CrawlerSeat> getAll() {
        List<CrawlerSeat> flushResult = new ArrayList<>();
        pinSeatBatch.drainTo(flushResult);
        return flushResult;
    }
}

package kr.allcll.backend.admin.seat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import kr.allcll.crawler.seat.CrawlerSeat;
import org.springframework.stereotype.Component;

@Component
public class GeneralSeatBatch {

    private final BlockingQueue<CrawlerSeat> generalSeatBatch;

    public GeneralSeatBatch() {
        this.generalSeatBatch = new LinkedBlockingQueue<>();
    }

    public void saveGeneralSeatToBatch(CrawlerSeat crawlerSeat) {
        generalSeatBatch.add(crawlerSeat);
    }

    public List<CrawlerSeat> getAll() {
        List<CrawlerSeat> flushResult = new ArrayList<>();
        generalSeatBatch.drainTo(flushResult);
        return flushResult;
    }
}

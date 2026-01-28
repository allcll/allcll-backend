package kr.allcll.backend.support.batch;

import kr.allcll.backend.admin.seat.GeneralSeatBatch;
import kr.allcll.backend.admin.seat.PinSeatBatch;
import kr.allcll.crawler.seat.CrawlerSeat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BatchService {

    private final PinSeatBatch pinSeatBatch;
    private final GeneralSeatBatch generalSeatBatch;

    public void savePinSeatBatch(CrawlerSeat renewedCrawlerSeat) {
        pinSeatBatch.add(renewedCrawlerSeat);
    }

    public void saveGeneralSeatBatch(CrawlerSeat renewedCrawlerSeat) {
        generalSeatBatch.add(renewedCrawlerSeat);
    }

    public void flushAllBatch() {
        pinSeatBatch.flush();
        generalSeatBatch.flush();
    }
}

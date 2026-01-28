package kr.allcll.backend.fixture;

import kr.allcll.crawler.seat.CrawlerSeat;
import kr.allcll.crawler.subject.CrawlerSubject;

public class CrawlerSeatFixture {

    public static CrawlerSeat createCrawlerSeat(CrawlerSubject crawlerSubject) {
        return new CrawlerSeat(null, crawlerSubject, "", null, null,
            null, "", null, null, null, "",
            null, null, null, "", null, null,
            null, null, null, null, "",
            "", "", "");
    }

}

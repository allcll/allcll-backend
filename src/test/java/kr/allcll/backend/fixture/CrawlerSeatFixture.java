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

    public static CrawlerSeat createCrawlerSeatWithLimits(
        Integer grade4Limit,
        Integer grade3Limit,
        Integer grade2Limit,
        Integer grade1Limit,
        Integer totalLimit,
        Integer totalRegisteredCount
    ) {
        return new CrawlerSeat(null, null, "", null, grade1Limit,
            null, "", grade4Limit, grade3Limit, grade2Limit, "",
            null, null, totalRegisteredCount, "", null, null,
            totalLimit, null, null, null, "",
            "", "", "");
    }

}

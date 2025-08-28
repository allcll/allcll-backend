package kr.allcll.backend.admin.preseat;

import kr.allcll.crawler.seat.CrawlerSeat;

public class PreSeatUtils {

    public static Integer getRemainPreSeat(CrawlerSeat crawlerSeat) {
        return Math.max(0, crawlerSeat.getTotLimitRcnt() - crawlerSeat.getTotRcnt());
        //return Math.max(0, crawlerSeat.getRemainTotRcnt()); 과 동일합니다.
    }
}

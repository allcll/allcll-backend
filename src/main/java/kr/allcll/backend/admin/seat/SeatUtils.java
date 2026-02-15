package kr.allcll.backend.admin.seat;

import kr.allcll.crawler.seat.CrawlerSeat;

public class SeatUtils {

    public static Integer getRemainSeat(CrawlerSeat crawlerSeat) {
        return Math.max(0, crawlerSeat.getTotLimitRcnt() - crawlerSeat.getTotRcnt());
    }
}

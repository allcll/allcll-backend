package kr.allcll.backend.admin.seat;

import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import kr.allcll.crawler.seat.CrawlerSeat;

public class SeatUtils {

    public static Integer getRemainSeat(CrawlerSeat crawlerSeat, SeatUtilsType seatUtilsType) {
        Integer remainSeat = getRawRemainSeat(crawlerSeat, seatUtilsType);
        return Math.max(0, remainSeat);
    }

    private static Integer getRawRemainSeat(CrawlerSeat crawlerSeat, SeatUtilsType seatUtilsType) {
        if (seatUtilsType == SeatUtilsType.TOTAL) {
            return crawlerSeat.getTotLimitRcnt() - crawlerSeat.getTotRcnt();
        }
        if (seatUtilsType == SeatUtilsType.GRADE_4) {
            return crawlerSeat.getTotLimitRcnt4() - crawlerSeat.getTotRcnt();
        }
        if (seatUtilsType == SeatUtilsType.GRADE_3) {
            return crawlerSeat.getTotLimitRcnt3() - crawlerSeat.getTotRcnt();
        }
        if (seatUtilsType == SeatUtilsType.GRADE_2) {
            return crawlerSeat.getTotLimitRcnt2() - crawlerSeat.getTotRcnt();
        }
        if (seatUtilsType == SeatUtilsType.GRADE_1) {
            return crawlerSeat.getTotLimitRcnt1() - crawlerSeat.getTotRcnt();
        }
        throw new AllcllException(AllcllErrorCode.UNSUPPORTED_SEAT_LIMIT_TYPE);
    }
}

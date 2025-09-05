package kr.allcll.backend.domain.seat.dto;

import kr.allcll.crawler.seat.CrawlerSeat;

public record DeprecatedSeatSummaryResponse(
    Long subjectId,
    Integer totalSeat,
    Integer remainSeat,
    Integer takenSeat
) {

    public static DeprecatedSeatSummaryResponse from(CrawlerSeat crawlerSeat) {
        return new DeprecatedSeatSummaryResponse(
            crawlerSeat.getCrawlerSubject().getId(),
            crawlerSeat.getTotLimitRcnt(),
            Math.max(crawlerSeat.getTotLimitRcnt() - crawlerSeat.getTotRcnt(), 0),
            crawlerSeat.getTotRcnt()
        );
    }
}

package kr.allcll.backend.admin.seat.dto;

public record SeatCrawlingStatusResponse(
    boolean isActive
) {

    public static SeatCrawlingStatusResponse of(boolean isActive) {
        return new SeatCrawlingStatusResponse(isActive);
    }
}

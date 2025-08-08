package kr.allcll.backend.support.scheduler.dto;

public record SeatSchedulerStatusResponse(
    boolean isActive
) {

    public static SeatSchedulerStatusResponse of(boolean isActive) {
        return new SeatSchedulerStatusResponse(isActive);
    }
}

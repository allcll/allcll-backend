package kr.allcll.backend.support.scheduler.dto;

public record SeatSchedulerStatusResponse(
    boolean isSending
) {

    public static SeatSchedulerStatusResponse of(boolean isSending) {
        return new SeatSchedulerStatusResponse(isSending);
    }
}

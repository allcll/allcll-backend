package kr.allcll.backend.admin.seat.dto;

public record SeatStatusResponse(
    boolean isActive
) {

    public static SeatStatusResponse of(boolean isActive) {
        return new SeatStatusResponse(isActive);
    }
}

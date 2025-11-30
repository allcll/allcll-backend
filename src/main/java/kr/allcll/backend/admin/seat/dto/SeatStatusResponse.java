package kr.allcll.backend.admin.seat.dto;

public record SeatStatusResponse(
    String userId,
    boolean isActive
) {

    public static SeatStatusResponse of(String userId, boolean isActive) {
        return new SeatStatusResponse(userId, isActive);
    }
}

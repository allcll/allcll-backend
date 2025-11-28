package kr.allcll.backend.admin.seat.dto;

import java.util.List;

public record SeatStatusResponse(
    List<String> userId,
    boolean isActive
) {

    public static SeatStatusResponse of(List<String> userId, boolean isActive) {
        return new SeatStatusResponse(userId, isActive);
    }
}

package kr.allcll.backend.admin.seat.dto;

public record SeatStreamStatusResponse(
    String status,
    String message
) {

    public static SeatStreamStatusResponse of(String status, String message) {
        return new SeatStreamStatusResponse(status, message);
    }
}

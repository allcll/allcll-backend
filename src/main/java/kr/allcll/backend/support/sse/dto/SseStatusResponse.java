package kr.allcll.backend.support.sse.dto;

public record SseStatusResponse(
    String status,
    String message
) {

    public static SseStatusResponse of(String status, String message) {
        return new SseStatusResponse(status, message);
    }
}

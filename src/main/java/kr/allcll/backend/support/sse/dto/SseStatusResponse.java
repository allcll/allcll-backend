package kr.allcll.backend.support.sse.dto;

public record SseStatusResponse(
    boolean isConnected
) {
    public static SseStatusResponse of(boolean isConnected) {
        return new SseStatusResponse(isConnected);
    }
}

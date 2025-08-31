package kr.allcll.backend.support.sse;

public enum SseStatus {
    LIVE("실시간 여석을 제공중이에요"),
    PRESEAT("preseat 여석을 이용해주세요"),
    IDLE("실시간 여석 제공 전이에요. 서비스가 시작되면 알림을 드릴게요."),
    ERROR("2025-09-02 까지 서비스 점검 중이에요");

    private final String message;

    SseStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

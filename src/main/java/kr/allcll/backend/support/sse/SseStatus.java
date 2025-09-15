package kr.allcll.backend.support.sse;

public enum SseStatus {
    LIVE("과목의 여석을 실시간으로 볼 수 있어요."),
    PRESEAT("전체 학년 여석 탭을 이용해 주세요."),
    IDLE("과목 실시간 여석 제공 전이에요. 서비스가 시작되면 알림을 드릴게요."),
    ERROR("서비스 점검 중이에요. 조금만 기다려주세요.");

    private final String message;

    SseStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

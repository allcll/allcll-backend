package kr.allcll.backend.support.exception;

public enum AllcllErrorCode {

    SUBJECT_NOT_FOUND("존재하지 않는 과목 입니다. (subjectId: %d)"),

    PIN_LIMIT_EXCEEDED("이미 %d개의 핀을 등록했습니다."),
    DUPLICATE_PIN("%s은(는) 이미 핀 등록된 과목입니다."),
    PIN_SUBJECT_MISMATCH("핀에 등록된 과목이 아닙니다."),

    STAR_LIMIT_EXCEEDED("이미 %d개의 즐겨찾기를 등록했습니다."),
    DUPLICATE_STAR("%s은(는) 이미 핀 등록된 과목입니다."),
    STAR_SUBJECT_MISMATCH("즐겨찾기에 등록된 과목이 아닙니다."),

    SEMESTER_NOT_FOUND("현재 학기 정보가 없습니다. 관리자에게 문의해주세요."),

    TOKEN_NOT_FOUND("쿠키에 토큰이 존재하지 않습니다."),

    BASKET_NOT_FOUND("관심과목 정보가 존재하지 않습니다."),

    EXTERNAL_CONNECTION_TERMINATED("외부 서버와의 연결이 종료되었습니다."),

    TOKEN_INVALID("유효하지 않은 토큰입니다."),
    TIMETABLE_NOT_FOUND("시간표를 찾을 수 없습니다."),
    UNAUTHORIZED_ACCESS("접근 권한이 없습니다."),
    INVALID_SEMESTER("유효하지 않은 학기입니다."),
    CUSTOM_SCHEDULE_NOT_FOUND("커스텀 일정을 찾을 수 없습니다."),
    DUPLICATE_SCHEDULE("이미 시간표에 등록된 일정입니다."),
    JSON_CONVERT_ERROR("TimeSlot JSON 변환 중 오류가 발생했습니다."),
    INVALID_SCHEDULE_TYPE("유효하지 않은 일정 타입입니다."),
    INVALID_TIME("시작 시간이 종료 시간과 같거나 늦습니다."),
    INVALID_SERVICE_TYPE("유효하지 않은 서비스 기간 타입입니다.");
    SEAT_CRAWLING_ALREADY_IN_PROGRESS("이미 다른 계정으로 여석 크롤링이 진행중입니다."),
    SEAT_CRAWLING_IN_MULTIPLE_ACCOUNTS("두 개 이상의 계정으로 여석 크롤링이 진행중입니다."),
    PREFIX_NOT_FOUND("prefix가 존재하지 않습니다."),
    ;

    private String message;

    AllcllErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

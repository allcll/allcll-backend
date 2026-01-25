package kr.allcll.backend.support.exception;

import org.springframework.http.HttpStatus;

public enum AllcllErrorCode {
    /* ================= 비즈니스 에러 ================= */
    //400
    INVALID_SEMESTER(HttpStatus.BAD_REQUEST, "유효하지 않은 학기입니다."),
    INVALID_SCHEDULE_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 일정 타입입니다."),
    INVALID_TIME(HttpStatus.BAD_REQUEST, "시작 시간이 종료 시간과 같거나 늦습니다."),
    PIN_SUBJECT_MISMATCH(HttpStatus.BAD_REQUEST, "핀에 등록된 과목이 아닙니다."),
    STAR_SUBJECT_MISMATCH(HttpStatus.BAD_REQUEST, "즐겨찾기에 등록된 과목이 아닙니다."),

    //401
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "쿠키에 토큰이 존재하지 않습니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),

    //403
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    //404
    SUBJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 과목 입니다. (subjectId: %d)"),
    TIMETABLE_NOT_FOUND(HttpStatus.NOT_FOUND, "시간표를 찾을 수 없습니다."),
    CUSTOM_SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "커스텀 일정을 찾을 수 없습니다."),
    BASKET_NOT_FOUND(HttpStatus.NOT_FOUND, "관심과목 정보가 존재하지 않습니다."),

    //409
    PIN_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "이미 %d개의 핀을 등록했습니다."),
    DUPLICATE_PIN(HttpStatus.CONFLICT, "%s은(는) 이미 핀 등록된 과목입니다."),
    DUPLICATE_STAR(HttpStatus.CONFLICT, "%s은(는) 이미 핀 등록된 과목입니다."),
    STAR_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "이미 %d개의 즐겨찾기를 등록했습니다."),
    DUPLICATE_SCHEDULE(HttpStatus.CONFLICT, "이미 시간표에 등록된 일정입니다."),
    SEAT_CRAWLING_ALREADY_IN_PROGRESS(HttpStatus.CONFLICT, "이미 다른 계정으로 여석 크롤링이 진행중입니다."),
    SEAT_CRAWLING_IN_MULTIPLE_ACCOUNTS(HttpStatus.CONFLICT, "두 개 이상의 계정으로 여석 크롤링이 진행중입니다."),

    //500
    SEMESTER_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "현재 학기 정보가 없습니다. 관리자에게 문의해주세요."),
    JSON_CONVERT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "TimeSlot JSON 변환 중 오류가 발생했습니다."),
    PREFIX_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "prefix가 존재하지 않습니다."),

    //502
    EXTERNAL_CONNECTION_TERMINATED(HttpStatus.BAD_GATEWAY, "외부 서버와의 연결이 종료되었습니다."),

    /* ================= 시스템 / 인프라 에러 ================= */
    NOT_FOUND_API(HttpStatus.NOT_FOUND, "요청한 API를 찾을 수 없습니다."),
    ASYNC_REQUEST_TIMEOUT(HttpStatus.INTERNAL_SERVER_ERROR, "요청 처리 중 시간이 초과되었습니다."),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러가 발생하였습니다.");;

    private final HttpStatus httpStatus;
    private final String message;

    AllcllErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }
}

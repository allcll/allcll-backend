package kr.allcll.backend.support.exception;

import org.springframework.http.ResponseEntity;

public record ErrorResponse(
    String code,
    String message
) {

    public static ResponseEntity<ErrorResponse> of(AllcllErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getHttpStatus()).
            body(new ErrorResponse(errorCode.name(), errorCode.getMessage()));
    }
}

package kr.allcll.backend.support.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public record ErrorResponse(
    String code,
    String message
) {

    public static ResponseEntity<ErrorResponse> of(final HttpStatus httpStatus, String code, String message) {
        return ResponseEntity.status(httpStatus).body(new ErrorResponse(code, message));
    }

}

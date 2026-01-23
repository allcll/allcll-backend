package kr.allcll.backend.support.exception;

public record ErrorResponse(
    String code,
    String message
) {

}

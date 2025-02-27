package kr.allcll.backend.exception;

public record ErrorResponse(
    String code,
    String message,
    String status
) {

}

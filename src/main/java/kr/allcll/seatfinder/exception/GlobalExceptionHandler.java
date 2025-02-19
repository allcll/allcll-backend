package kr.allcll.seatfinder.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String LOG_FORMAT = """
        \n\t{
            "RequestURI": "{} {}",
            "RequestBody": {},
            "ErrorMessage": "{}"
        \t}
        """;

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleAllcllException(HttpServletRequest request, AllcllException e) {
        log.warn(LOG_FORMAT, request.getMethod(), request.getRequestURI(), getRequestBody(request), e.getMessage());
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(e.getErrorCode(),
                e.getMessage(),
                HttpStatus.BAD_REQUEST.toString()));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleServletException(HttpServletRequest request, ServletException e) {
        log.warn(LOG_FORMAT, request.getMethod(), request.getRequestURI(), getRequestBody(request), e.getMessage());
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleException(HttpServletRequest request, Exception e) {
        log.error(LOG_FORMAT, request.getMethod(), request.getRequestURI(), getRequestBody(request), e.getMessage(), e);
        return ResponseEntity.internalServerError()
            .body(new ErrorResponse("SERVER_ERROR",
                "서버 에러가 발생하였습니다.",
                HttpStatus.INTERNAL_SERVER_ERROR.toString()));
    }

    private String getRequestBody(HttpServletRequest request) {
        try (BufferedReader reader = request.getReader()) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator() + "\t"));
        } catch (IOException e) {
            log.error("Failed to read request body", e);
            return "";
        }
    }
}

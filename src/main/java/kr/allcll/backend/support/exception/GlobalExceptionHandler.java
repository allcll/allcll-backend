package kr.allcll.backend.support.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

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
    public ResponseEntity<ErrorResponse> handleAllcllException(HttpServletRequest request, AllcllException exception) {
        final AllcllErrorCode errorCode = exception.getErrorCode();

        log.warn(LOG_FORMAT, request.getMethod(), request.getRequestURI(), getRequestBody(request),
            exception.getMessage());
        return ErrorResponse.of(errorCode);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleServletException(HttpServletRequest request,
        ServletException exception) {
        final AllcllErrorCode errorCode = AllcllErrorCode.NOT_FOUND_API;

        log.info(LOG_FORMAT, request.getMethod(), request.getRequestURI(), getRequestBody(request),
            exception.getMessage());
        return ErrorResponse.of(errorCode);
    }

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleAsyncRequestTimeoutException(HttpServletRequest request) {
        final AllcllErrorCode errorCode = AllcllErrorCode.ASYNC_REQUEST_TIMEOUT;

        if (request.getHeader("ALLCLL-SSE-CONNECT") != null) {
            log.info("SSE connection timed out (normal close) - {} {}", request.getMethod(), request.getRequestURI());
            return ResponseEntity.noContent().build();
        }
        return ErrorResponse.of(errorCode);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleException(HttpServletRequest request, Exception exception) {
        final AllcllErrorCode errorCode = AllcllErrorCode.SERVER_ERROR;

        log.error(LOG_FORMAT, request.getMethod(), request.getRequestURI(), getRequestBody(request),
            exception.getMessage(), exception);
        return ErrorResponse.of(errorCode);
    }

    private String getRequestBody(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType != null && contentType.startsWith("multipart/")) {
            return "[multipart/form-data]";
        }
        try (BufferedReader reader = request.getReader()) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator() + "\t"));
        } catch (IOException | java.io.UncheckedIOException e) {
            log.error("Failed to read request body", e);
            return "";
        }
    }
}

package kr.allcll.backend.support.exception;

import io.sentry.Sentry;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 자격 증명을 본문으로 받는 경로. 예외 발생 시 본문을 그대로 로깅하면 비밀번호와 세션 토큰이 로그와 Sentry 에 남는다.
     * <p>
     * 주의: 크롤러 모듈의 {@code CrawlerGlobalExceptionHandler} 도 같은 컨텍스트에 스캔되어 본문을 로깅한다. 그쪽은 수정할 수 없으므로, 자격 증명을 다루는 코드는 크롤러 예외를 그대로
     * 흘려보내지 말고 {@link AllcllException} 으로 변환해 이 핸들러가 처리하게 해야 한다.
     */
    private static final Set<String> CREDENTIAL_REQUEST_URIS = Set.of(
        "/api/auth/login",
        "/api/admin/session",
        "/api/admin/session/sso"
    );
    private static final String MASKED_BODY = "[redacted]";

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

        if (errorCode.getHttpStatus().is5xxServerError()) {
            captureException(request, exception, errorCode);
        }
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

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
        HttpServletRequest request,
        MethodArgumentNotValidException exception
    ) {
        final AllcllErrorCode errorCode = AllcllErrorCode.INVALID_REQUEST_VALUE;

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

        captureException(request, exception, errorCode);
        log.error(LOG_FORMAT, request.getMethod(), request.getRequestURI(), getRequestBody(request),
            exception.getMessage(), exception);
        return ErrorResponse.of(errorCode);
    }

    private void captureException(
        HttpServletRequest request,
        Exception exception,
        AllcllErrorCode errorCode
    ) {
        Sentry.withScope(scope -> {
            scope.setTag("method", request.getMethod());
            scope.setTag("path", request.getRequestURI());
            scope.setTag("status", String.valueOf(errorCode.getHttpStatus().value()));
            scope.setTag("errorCode", errorCode.name());
            scope.setTag("exceptionType", exception.getClass().getSimpleName());
            Sentry.captureException(exception);
        });
    }

    private String getRequestBody(HttpServletRequest request) {
        if (CREDENTIAL_REQUEST_URIS.contains(request.getRequestURI())) {
            return MASKED_BODY;
        }
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

package kr.allcll.backend.support.exception;

import io.sentry.Sentry;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.servlet.HandlerMapping;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String LOG_FORMAT =
        "Request failed: method={} route={} status={} errorCode={} exceptionType={}";

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleAllcllException(HttpServletRequest request, AllcllException exception) {
        final AllcllErrorCode errorCode = exception.getErrorCode();

        if (errorCode.getHttpStatus().is5xxServerError()) {
            captureException(request, exception, errorCode);
        }
        log.warn(LOG_FORMAT, request.getMethod(), routeTemplate(request), errorCode.getHttpStatus().value(),
            errorCode.name(), exception.getClass().getSimpleName());
        return ErrorResponse.of(errorCode);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleServletException(HttpServletRequest request,
        ServletException exception) {
        final AllcllErrorCode errorCode = AllcllErrorCode.NOT_FOUND_API;

        log.info(LOG_FORMAT, request.getMethod(), routeTemplate(request), errorCode.getHttpStatus().value(),
            errorCode.name(), exception.getClass().getSimpleName());
        return ErrorResponse.of(errorCode);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
        HttpServletRequest request,
        MethodArgumentNotValidException exception
    ) {
        final AllcllErrorCode errorCode = AllcllErrorCode.INVALID_REQUEST_VALUE;

        log.info(LOG_FORMAT, request.getMethod(), routeTemplate(request), errorCode.getHttpStatus().value(),
            errorCode.name(), exception.getClass().getSimpleName());
        return ErrorResponse.of(errorCode);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
        HttpServletRequest request,
        HttpMessageNotReadableException exception
    ) {
        final AllcllErrorCode errorCode = AllcllErrorCode.INVALID_REQUEST_VALUE;

        log.info(LOG_FORMAT, request.getMethod(), routeTemplate(request), errorCode.getHttpStatus().value(),
            errorCode.name(), exception.getClass().getSimpleName());
        return ErrorResponse.of(errorCode);
    }

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleAsyncRequestTimeoutException(HttpServletRequest request) {
        final AllcllErrorCode errorCode = AllcllErrorCode.ASYNC_REQUEST_TIMEOUT;

        if (request.getHeader("ALLCLL-SSE-CONNECT") != null) {
            log.info("SSE connection timed out (normal close): method={} route={} status={} errorCode={}",
                request.getMethod(), routeTemplate(request), errorCode.getHttpStatus().value(), errorCode.name());
            return ResponseEntity.noContent().build();
        }
        return ErrorResponse.of(errorCode);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleException(HttpServletRequest request, Exception exception) {
        final AllcllErrorCode errorCode = AllcllErrorCode.SERVER_ERROR;

        captureException(request, exception, errorCode);
        log.error(LOG_FORMAT, request.getMethod(), routeTemplate(request), errorCode.getHttpStatus().value(),
            errorCode.name(), exception.getClass().getSimpleName());
        return ErrorResponse.of(errorCode);
    }

    private void captureException(
        HttpServletRequest request,
        Exception exception,
        AllcllErrorCode errorCode
    ) {
        Sentry.withScope(scope -> {
            scope.setTag("method", request.getMethod());
            scope.setTag("path", routeTemplate(request));
            scope.setTag("status", String.valueOf(errorCode.getHttpStatus().value()));
            scope.setTag("errorCode", errorCode.name());
            scope.setTag("exceptionType", exception.getClass().getSimpleName());
            Sentry.captureException(exception);
        });
    }

    private String routeTemplate(HttpServletRequest request) {
        Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (pattern instanceof String routeTemplate) {
            return routeTemplate;
        }
        return "[unmatched]";
    }
}

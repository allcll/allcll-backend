package kr.allcll.backend.support.exception;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import io.sentry.IScope;
import io.sentry.ScopeCallback;
import io.sentry.Sentry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("4xx AllcllException은 Sentry로 전송하지 않는다")
    void doesNotCaptureClientAllcllException() {
        // Given
        MockHttpServletRequest request = createRequest();
        AllcllException exception = new AllcllException(AllcllErrorCode.INVALID_REQUEST_VALUE);

        try (MockedStatic<Sentry> sentry = mockStatic(Sentry.class)) {
            // When
            exceptionHandler.handleAllcllException(request, exception);

            // Then
            sentry.verify(() -> Sentry.withScope(any(ScopeCallback.class)), never());
            sentry.verify(() -> Sentry.captureException(any(Throwable.class)), never());
        }
    }

    @Test
    @DisplayName("5xx AllcllException은 Sentry로 전송한다")
    void capturesServerAllcllException() {
        // Given
        MockHttpServletRequest request = createRequest();
        AllcllException exception = new AllcllException(AllcllErrorCode.SEMESTER_NOT_FOUND);

        verifyCapturedException(request, exception, AllcllErrorCode.SEMESTER_NOT_FOUND);
    }

    @Test
    @DisplayName("502 AllcllException도 Sentry로 전송한다")
    void capturesBadGatewayAllcllException() {
        // Given
        MockHttpServletRequest request = createRequest();
        AllcllException exception = new AllcllException(AllcllErrorCode.EXTERNAL_CONNECTION_TERMINATED);

        verifyCapturedException(request, exception, AllcllErrorCode.EXTERNAL_CONNECTION_TERMINATED);
    }

    @Test
    @DisplayName("예상하지 못한 일반 Exception은 SERVER_ERROR로 Sentry에 전송한다")
    void capturesUnexpectedException() {
        // Given
        MockHttpServletRequest request = createRequest();
        Exception exception = new IllegalStateException("unexpected");

        verifyCapturedException(request, exception, AllcllErrorCode.SERVER_ERROR);
    }

    private void verifyCapturedException(
        MockHttpServletRequest request,
        Exception exception,
        AllcllErrorCode errorCode
    ) {
        IScope scope = mock(IScope.class);

        try (MockedStatic<Sentry> sentry = mockStatic(Sentry.class)) {
            sentry.when(() -> Sentry.withScope(any(ScopeCallback.class)))
                .thenAnswer(invocation -> {
                    ScopeCallback callback = invocation.getArgument(0);
                    callback.run(scope);
                    return null;
                });

            // When
            if (exception instanceof AllcllException allcllException) {
                exceptionHandler.handleAllcllException(request, allcllException);
            } else {
                exceptionHandler.handleException(request, exception);
            }

            // Then
            verify(scope).setTag("method", "POST");
            verify(scope).setTag("path", "/api/test");
            verify(scope).setTag("status", String.valueOf(errorCode.getHttpStatus().value()));
            verify(scope).setTag("errorCode", errorCode.name());
            verify(scope).setTag("exceptionType", exception.getClass().getSimpleName());
            verifyNoMoreInteractions(scope);
            sentry.verify(() -> Sentry.captureException(exception));
        }
    }

    private MockHttpServletRequest createRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/test");
        request.setQueryString("token=secret");
        request.addHeader("Authorization", "Bearer secret");
        request.setCookies(new jakarta.servlet.http.Cookie("accessToken", "secret"));
        request.setContent("{\"password\":\"secret\"}".getBytes());
        return request;
    }
}

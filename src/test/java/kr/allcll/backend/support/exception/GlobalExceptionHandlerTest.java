package kr.allcll.backend.support.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.sentry.IScope;
import io.sentry.ScopeCallback;
import io.sentry.Sentry;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

    private static final String PASSWORD = "sup3r-secret-pw";

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
    private final ListAppender<ILoggingEvent> logs = new ListAppender<>();
    private Logger handlerLogger;

    @BeforeEach
    void attachLogAppender() {
        handlerLogger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        logs.start();
        handlerLogger.addAppender(logs);
    }

    @AfterEach
    void detachLogAppender() {
        handlerLogger.detachAppender(logs);
        logs.stop();
    }

    @Test
    @DisplayName("포털 로그인이 실패해도 비밀번호가 로그에 남지 않는다")
    void doesNotLogPasswordOnPortalLoginFailure() {
        // Given
        MockHttpServletRequest request = credentialRequest(
            "/api/auth/login",
            "{\"studentId\":\"21011138\",\"password\":\"" + PASSWORD + "\"}"
        );

        // When
        exceptionHandler.handleException(request, new IllegalStateException("boom"));

        // Then
        assertThat(capturedLogs()).doesNotContain(PASSWORD);
    }

    @Test
    @DisplayName("SSO 로그인이 실패해도 비밀번호가 로그에 남지 않는다")
    void doesNotLogPasswordOnSsoLoginFailure() {
        // Given
        MockHttpServletRequest request = credentialRequest(
            "/api/admin/session/sso",
            "{\"id\":\"21011138\",\"password\":\"" + PASSWORD + "\"}"
        );

        // When
        exceptionHandler.handleException(request, new IllegalStateException("boom"));

        // Then
        assertThat(capturedLogs()).doesNotContain(PASSWORD);
    }

    @Test
    @DisplayName("자격 증명 요청도 어떤 경로에서 실패했는지는 로그에 남는다")
    void stillLogsRequestUriForCredentialRequest() {
        // Given
        MockHttpServletRequest request = credentialRequest("/api/auth/login", "{\"password\":\"" + PASSWORD + "\"}");

        // When
        exceptionHandler.handleException(request, new IllegalStateException("boom"));

        // Then
        assertThat(capturedLogs()).contains("/api/auth/login");
    }

    @Test
    @DisplayName("자격 증명과 무관한 요청은 기존대로 본문을 로그에 남긴다")
    void keepsLoggingBodyForOrdinaryRequest() {
        // Given
        MockHttpServletRequest request = credentialRequest("/api/admin/subjects", "{\"year\":\"2026\"}");

        // When
        exceptionHandler.handleException(request, new IllegalStateException("boom"));

        // Then
        assertThat(capturedLogs()).contains("2026");
    }

    private MockHttpServletRequest credentialRequest(String uri, String body) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", uri);
        request.setContentType("application/json");
        request.setContent(body.getBytes(StandardCharsets.UTF_8));
        return request;
    }

    private String capturedLogs() {
        return logs.list.stream()
            .map(ILoggingEvent::getFormattedMessage)
            .collect(Collectors.joining(System.lineSeparator()));
    }

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

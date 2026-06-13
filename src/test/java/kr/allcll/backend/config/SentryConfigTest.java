package kr.allcll.backend.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.sentry.Hint;
import io.sentry.SentryEvent;
import io.sentry.SentryOptions;
import io.sentry.protocol.Request;
import io.sentry.protocol.User;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SentryConfigTest {

    private final SentryConfig sentryConfig = new SentryConfig();

    @Test
    @DisplayName("SENTRY_DSN이 없으면 Sentry 전송을 비활성화한다")
    void configureSentryOptionsWithoutDsn() {
        // Given
        SentryOptions options = new SentryOptions();

        // When
        sentryConfig.configureSentryOptions(options, " ");

        // Then
        assertThat(options.isEnabled()).isFalse();
        assertThat(options.getDsn()).isNull();
        assertThat(options.isSendDefaultPii()).isFalse();
        assertThat(options.getMaxRequestBodySize()).isEqualTo(SentryOptions.RequestSize.NONE);
    }

    @Test
    @DisplayName("SENTRY_DSN이 있으면 Sentry 전송을 활성화한다")
    void configureSentryOptionsWithDsn() {
        // Given
        SentryOptions options = new SentryOptions();

        // When
        sentryConfig.configureSentryOptions(options, " https://public-key@example.ingest.sentry.io/project-id ");

        // Then
        assertThat(options.isEnabled()).isTrue();
        assertThat(options.getDsn()).isEqualTo("https://public-key@example.ingest.sentry.io/project-id");
        assertThat(options.isSendDefaultPii()).isFalse();
        assertThat(options.getMaxRequestBodySize()).isEqualTo(SentryOptions.RequestSize.NONE);
    }

    @Test
    @DisplayName("Sentry 이벤트 전송 전 요청 본문과 개인정보를 제거한다")
    void removeSensitiveData() {
        // Given
        SentryEvent event = new SentryEvent();
        event.setUser(new User());

        Request request = new Request();
        request.setData(Map.of("password", "secret"));
        request.setCookies("accessToken=token");
        request.setQueryString("token=secret");
        request.setHeaders(Map.of("Authorization", "Bearer token"));
        event.setRequest(request);

        // When
        SentryEvent sanitizedEvent = sentryConfig.removeSensitiveData(event, new Hint());

        // Then
        assertThat(sanitizedEvent.getUser()).isNull();
        assertThat(sanitizedEvent.getRequest().getData()).isNull();
        assertThat(sanitizedEvent.getRequest().getCookies()).isNull();
        assertThat(sanitizedEvent.getRequest().getQueryString()).isNull();
        assertThat(sanitizedEvent.getRequest().getHeaders()).isNull();
    }
}

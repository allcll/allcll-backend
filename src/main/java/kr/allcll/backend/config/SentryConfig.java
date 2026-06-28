package kr.allcll.backend.config;

import io.sentry.Hint;
import io.sentry.Sentry;
import io.sentry.SentryEvent;
import io.sentry.SentryOptions;
import io.sentry.protocol.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class SentryConfig {

    @Bean
    public Sentry.OptionsConfiguration<SentryOptions> sentryOptionsConfiguration(
        @Value("${SENTRY_DSN:}") String sentryDsn
    ) {
        return options -> configureSentryOptions(options, sentryDsn);
    }

    @Bean
    public SentryOptions.BeforeSendCallback sentryBeforeSendCallback() {
        return this::removeSensitiveData;
    }

    void configureSentryOptions(SentryOptions options, String sentryDsn) {
        options.setSendDefaultPii(false);
        options.setMaxRequestBodySize(SentryOptions.RequestSize.NONE);

        if (StringUtils.hasText(sentryDsn)) {
            options.setDsn(sentryDsn.trim());
            options.setEnabled(true);
            return;
        }

        options.setDsn(null);
        options.setEnabled(false);
    }

    SentryEvent removeSensitiveData(SentryEvent event, Hint hint) {
        event.setUser(null);

        Request request = event.getRequest();
        if (request != null) {
            request.setData(null);
            request.setCookies(null);
            request.setQueryString(null);
            request.setHeaders(null);
        }

        return event;
    }
}

package kr.allcll.backend.support.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.LoggingEvent;
import jakarta.servlet.ServletException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestIdFilterTest {

    private final RequestIdFilter requestIdFilter = new RequestIdFilter();

    @AfterEach
    void tearDown() {
        MDC.remove(RequestIdFilter.REQUEST_ID_MDC_KEY);
    }

    @Test
    void 요청_처리_중에는_서버가_생성한_12자리_requestId가_MDC에_있고_응답은_그대로_전달된다() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Request-Id", "client-supplied-request-id");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> requestIdDuringRequest = new AtomicReference<>();

        requestIdFilter.doFilter(request, response, (servletRequest, servletResponse) -> {
            requestIdDuringRequest.set(MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY));
            response.setStatus(HttpStatus.CREATED.value());
            response.getWriter().write("unchanged-response");
        });

        assertThat(requestIdDuringRequest.get()).isNotNull().isNotEqualTo("client-supplied-request-id");
        assertThat(requestIdDuringRequest.get()).matches("[A-Za-z0-9_-]{12}");
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.getContentAsString()).isEqualTo("unchanged-response");
        assertThat(MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY)).isNull();
    }

    @Test
    void 요청_처리_중_예외가_발생해도_requestId를_MDC에서_제거한다() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> requestIdFilter.doFilter(request, response,
            (servletRequest, servletResponse) -> {
                throw new ServletException("request failed");
            }))
            .isInstanceOf(ServletException.class);

        assertThat(MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY)).isNull();
    }

    @Test
    void 짧은_requestId는_로그_패턴에_출력될_수_있다() {
        String requestId = "Cx8rP2mKq9Vd";
        MDC.put(RequestIdFilter.REQUEST_ID_MDC_KEY, requestId);

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayout layout = new PatternLayout();
        layout.setContext(loggerContext);
        layout.setPattern("[requestId=%X{requestId:-}] %msg%n");
        layout.start();

        Logger logger = loggerContext.getLogger(RequestIdFilterTest.class);
        LoggingEvent event = new LoggingEvent(
            RequestIdFilterTest.class.getName(), logger, Level.INFO, "request completed", null, null
        );

        assertThat(layout.doLayout(event))
            .isEqualTo("[requestId=" + requestId + "] request completed" + System.lineSeparator());

        layout.stop();
    }
}

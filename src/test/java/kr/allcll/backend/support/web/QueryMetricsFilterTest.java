package kr.allcll.backend.support.web;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import kr.allcll.backend.support.metrics.QueryMetrics;
import kr.allcll.backend.support.metrics.QueryStatsSessionListener;
import kr.allcll.backend.support.metrics.RequestQueryStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

class QueryMetricsFilterTest {

    private MeterRegistry meterRegistry;
    private QueryMetricsFilter queryMetricsFilter;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        queryMetricsFilter = new QueryMetricsFilter(new QueryMetrics(meterRegistry));
    }

    @Test
    @DisplayName("요청이 실행한 SQL 수와 시간을 핸들러 매핑 패턴 태그로 기록한다.")
    void recordQueryStatsWithUriTemplateTag() throws Exception {
        // given
        MockHttpServletRequest request = requestWithUriTemplate("GET", "/api/baskets/{subjectId}");

        // when
        queryMetricsFilter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
            executeStatement();
            executeStatement();
        });

        // then
        DistributionSummary queryCount = findQueryCount("GET", "/api/baskets/{subjectId}");
        assertThat(queryCount).isNotNull();
        assertThat(queryCount.count()).isEqualTo(1);
        assertThat(queryCount.totalAmount()).isEqualTo(2.0);

        Timer queryTime = findQueryTime("GET", "/api/baskets/{subjectId}");
        assertThat(queryTime).isNotNull();
        assertThat(queryTime.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("경로가 같아도 HTTP method 가 다르면 따로 기록한다.")
    void recordSameUriPerHttpMethod() throws Exception {
        // given
        MockHttpServletRequest getRequest = requestWithUriTemplate("GET", "/api/timetables");
        MockHttpServletRequest postRequest = requestWithUriTemplate("POST", "/api/timetables");

        // when
        queryMetricsFilter.doFilter(getRequest, new MockHttpServletResponse(), (req, res) -> executeStatement());
        queryMetricsFilter.doFilter(postRequest, new MockHttpServletResponse(), (req, res) -> {
            executeStatement();
            executeStatement();
            executeStatement();
        });

        // then
        assertThat(findQueryCount("GET", "/api/timetables").totalAmount()).isEqualTo(1.0);
        assertThat(findQueryCount("POST", "/api/timetables").totalAmount()).isEqualTo(3.0);
    }

    @Test
    @DisplayName("핸들러가 매칭되지 않은 요청은 기록하지 않는다.")
    void skipUnmappedRequest() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/존재하지않는경로");

        // when
        queryMetricsFilter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
        });

        // then
        assertThat(meterRegistry.find("db.query.count").summaries()).isEmpty();
        assertThat(meterRegistry.find("db.query.time").timers()).isEmpty();
    }

    @Test
    @DisplayName("요청 처리 중 예외가 나도 집계를 정리해 다음 요청에 값이 새지 않는다.")
    void clearStatsWhenRequestFails() {
        // given
        MockHttpServletRequest request = requestWithUriTemplate("GET", "/api/subjects");

        // when
        try {
            queryMetricsFilter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
                executeStatement();
                throw new IllegalStateException("조회 실패");
            });
        } catch (Exception ignored) {
        }

        // then
        assertThat(findQueryCount("GET", "/api/subjects").totalAmount()).isEqualTo(1.0);
        assertThat(RequestQueryStats.stop().statementCount()).isZero();
    }

    private void executeStatement() {
        QueryStatsSessionListener listener = new QueryStatsSessionListener();
        listener.jdbcExecuteStatementStart();
        listener.jdbcExecuteStatementEnd();
    }

    private MockHttpServletRequest requestWithUriTemplate(String httpMethod, String uriTemplate) {
        MockHttpServletRequest request = new MockHttpServletRequest(httpMethod, uriTemplate);
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, uriTemplate);
        return request;
    }

    private DistributionSummary findQueryCount(String httpMethod, String uri) {
        return meterRegistry.find("db.query.count").tag("method", httpMethod).tag("uri", uri).summary();
    }

    private Timer findQueryTime(String httpMethod, String uri) {
        return meterRegistry.find("db.query.time").tag("method", httpMethod).tag("uri", uri).timer();
    }
}

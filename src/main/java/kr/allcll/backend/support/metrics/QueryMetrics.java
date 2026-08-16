package kr.allcll.backend.support.metrics;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

/**
 * 요청 하나가 DB에 쓴 비용을 엔드포인트별로 기록한다.
 *
 * - 메트릭 두 개를 노출한다.
 *  # 요청당 SQL문 수(db_query_count)
 *  # 요청당 SQL 실행 시간(db_query_time_seconds)
 */

@Component
public class QueryMetrics {

    private static final String QUERY_COUNT_METER_NAME = "db.query.count";
    private static final String QUERY_TIME_METER_NAME = "db.query.time";
    private static final String METHOD_TAG = "method";
    private static final String URI_TAG = "uri";

    private static final double[] QUERY_COUNT_BUCKETS = {1, 2, 3, 5, 10, 20, 50, 100, 500, 1_000, 5_000};

    private static final Duration[] QUERY_TIME_BUCKETS = {
        Duration.ofMillis(1), Duration.ofMillis(2), Duration.ofMillis(5), Duration.ofMillis(10),
        Duration.ofMillis(20), Duration.ofMillis(50), Duration.ofMillis(100), Duration.ofMillis(200),
        Duration.ofMillis(500), Duration.ofSeconds(1), Duration.ofSeconds(2), Duration.ofSeconds(5)
    };

    private final MeterRegistry meterRegistry;
    private final Map<Endpoint, DistributionSummary> queryCountSummaries = new ConcurrentHashMap<>();
    private final Map<Endpoint, Timer> queryTimeTimers = new ConcurrentHashMap<>();

    public QueryMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void registerEndpoint(String httpMethod, String uriTemplate) {
        Endpoint endpoint = new Endpoint(httpMethod, uriTemplate);
        queryCountSummaries.computeIfAbsent(endpoint, this::registerQueryCountSummary);
        queryTimeTimers.computeIfAbsent(endpoint, this::registerQueryTimeTimer);
    }

    public void record(String httpMethod, String uriTemplate, RequestQueryStats queryStats) {
        Endpoint endpoint = new Endpoint(httpMethod, uriTemplate);
        queryCountSummaries.computeIfAbsent(endpoint, this::registerQueryCountSummary)
            .record(queryStats.statementCount());
        queryTimeTimers.computeIfAbsent(endpoint, this::registerQueryTimeTimer)
            .record(queryStats.executionNanos(), TimeUnit.NANOSECONDS);
    }

    private DistributionSummary registerQueryCountSummary(Endpoint endpoint) {
        return DistributionSummary.builder(QUERY_COUNT_METER_NAME)
            .description("요청 1건이 발행한 SQL 문 수")
            .tag(METHOD_TAG, endpoint.httpMethod())
            .tag(URI_TAG, endpoint.uriTemplate())
            .serviceLevelObjectives(QUERY_COUNT_BUCKETS)
            .register(meterRegistry);
    }

    private Timer registerQueryTimeTimer(Endpoint endpoint) {
        return Timer.builder(QUERY_TIME_METER_NAME)
            .description("요청 1건이 SQL 실행에 쓴 시간")
            .tag(METHOD_TAG, endpoint.httpMethod())
            .tag(URI_TAG, endpoint.uriTemplate())
            .serviceLevelObjectives(QUERY_TIME_BUCKETS)
            .register(meterRegistry);
    }

    private record Endpoint(String httpMethod, String uriTemplate) {

    }
}

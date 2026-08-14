package kr.allcll.backend.support.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class QueryMetricsIntegrationTest {

    private static final String NOTICES_URI = "/api/notices";
    private static final String NEVER_CALLED_URI = "/api/baskets";

    @LocalServerPort
    private int port;

    @Autowired
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("요청을 보내면 그 요청이 쓴 쿼리 수와 시간이 엔드포인트별로 기록된다.")
    void recordQueryStatsOnRealRequest() {
        // given
        RestAssured.given().when().get(NOTICES_URI).then().statusCode(200);

        // when
        DistributionSummary queryCount = meterRegistry.find("db.query.count").tag("uri", NOTICES_URI).summary();
        Timer queryTime = meterRegistry.find("db.query.time").tag("uri", NOTICES_URI).timer();

        // then
        assertThat(queryCount).isNotNull();
        assertThat(queryCount.count()).isPositive();
        assertThat(queryCount.totalAmount()).isPositive();
        assertThat(queryTime).isNotNull();
        assertThat(queryTime.count()).isPositive();
    }

    @Test
    @DisplayName("요청이 한 번도 없던 엔드포인트도 기동 시점에 미터가 등록돼 있다.")
    void registerEndpointBeforeFirstRequest() {
        // given
        // 이 테스트는 NEVER_CALLED_URI 로 요청을 보내지 않는다

        // when
        DistributionSummary queryCount = meterRegistry.find("db.query.count").tag("uri", NEVER_CALLED_URI).summary();
        Timer queryTime = meterRegistry.find("db.query.time").tag("uri", NEVER_CALLED_URI).timer();

        // then
        assertThat(queryCount).isNotNull();
        assertThat(queryCount.count()).isZero();
        assertThat(queryTime).isNotNull();
        assertThat(queryTime.count()).isZero();
    }
}

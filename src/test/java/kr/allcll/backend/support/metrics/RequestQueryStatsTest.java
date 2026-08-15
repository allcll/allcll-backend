package kr.allcll.backend.support.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RequestQueryStatsTest {

    private static final long ONE_MILLI_NANOS = 1_000_000L;

    @Test
    @DisplayName("집계를 시작한 뒤 실행한 SQL 수와 시간을 수집한다.")
    void collectCountAndTimeAfterStart() {
        // given
        RequestQueryStats.start();

        // when
        RequestQueryStats.recordStatement(ONE_MILLI_NANOS);
        RequestQueryStats.recordStatement(2 * ONE_MILLI_NANOS);

        // then
        RequestQueryStats queryStats = RequestQueryStats.stop();
        assertThat(queryStats.statementCount()).isEqualTo(2);
        assertThat(queryStats.executionNanos()).isEqualTo(3 * ONE_MILLI_NANOS);
    }

    @Test
    @DisplayName("집계를 시작하지 않은 스레드에서는 수집하지 않는다.")
    void collectNothingWithoutStart() {
        // given
        RequestQueryStats.recordStatement(ONE_MILLI_NANOS);

        // when
        RequestQueryStats queryStats = RequestQueryStats.stop();

        // then
        assertThat(queryStats.statementCount()).isZero();
        assertThat(queryStats.executionNanos()).isZero();
    }

    @Test
    @DisplayName("스레드마다 따로 수집하므로 동시 요청이 서로 섞이지 않는다.")
    void collectPerThread() {
        // given
        RequestQueryStats.start();
        RequestQueryStats.recordStatement(ONE_MILLI_NANOS);

        // when
        int otherThreadCount = CompletableFuture.supplyAsync(() -> {
            RequestQueryStats.start();
            RequestQueryStats.recordStatement(ONE_MILLI_NANOS);
            RequestQueryStats.recordStatement(ONE_MILLI_NANOS);
            return RequestQueryStats.stop().statementCount();
        }).join();

        // then
        assertThat(otherThreadCount).isEqualTo(2);
        assertThat(RequestQueryStats.stop().statementCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("집계를 끝내면 값이 남지 않아 스레드를 재사용해도 이어서 수집하지 않는다.")
    void resetAfterStop() {
        // given
        RequestQueryStats.start();
        RequestQueryStats.recordStatement(ONE_MILLI_NANOS);
        RequestQueryStats.stop();

        // when
        RequestQueryStats.recordStatement(ONE_MILLI_NANOS);

        // then
        assertThat(RequestQueryStats.stop().statementCount()).isZero();
    }
}

package kr.allcll.backend.support.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QueryStatsSessionListenerTest {

    @Test
    @DisplayName("SQL문 실행 시작과 종료 사이의 시간을 집계에 더한다.")
    void recordExecutionTime() {
        // given
        RequestQueryStats.start();
        QueryStatsSessionListener listener = new QueryStatsSessionListener();

        // when
        listener.jdbcExecuteStatementStart();
        listener.jdbcExecuteStatementEnd();

        // then
        RequestQueryStats queryStats = RequestQueryStats.stop();
        assertThat(queryStats.statementCount()).isEqualTo(1);
        assertThat(queryStats.executionMillis()).isNotNegative();
    }

    @Test
    @DisplayName("한 세션에서 쿼리문을 여러 번 실행하면 실행 수만큼 집계된다.")
    void recordEveryStatementInSession() {
        // given
        RequestQueryStats.start();
        QueryStatsSessionListener listener = new QueryStatsSessionListener();

        // when
        listener.jdbcExecuteStatementStart();
        listener.jdbcExecuteStatementEnd();
        listener.jdbcExecuteStatementStart();
        listener.jdbcExecuteStatementEnd();
        listener.jdbcExecuteStatementStart();
        listener.jdbcExecuteStatementEnd();

        // then
        assertThat(RequestQueryStats.stop().statementCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("집계를 시작하지 않은 스레드의 실행은 무시한다.")
    void ignoreExecutionWithoutStart() {
        // given
        QueryStatsSessionListener listener = new QueryStatsSessionListener();

        // when
        listener.jdbcExecuteStatementStart();
        listener.jdbcExecuteStatementEnd();

        // then
        assertThat(RequestQueryStats.stop().statementCount()).isZero();
    }
}

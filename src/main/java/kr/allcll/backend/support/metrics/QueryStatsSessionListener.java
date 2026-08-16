package kr.allcll.backend.support.metrics;

import org.hibernate.SessionEventListener;

/**
 * Hibernate 가 JDBC 문을 실행할 때마다 실행 시간을 RequestQueryStats에 수집한다.
 */
public class QueryStatsSessionListener implements SessionEventListener {

    private long statementStartNanos;
    private long batchStartNanos;

    @Override
    public void jdbcExecuteStatementStart() {
        statementStartNanos = System.nanoTime();
    }

    @Override
    public void jdbcExecuteStatementEnd() {
        RequestQueryStats.recordStatement(System.nanoTime() - statementStartNanos);
    }

    @Override
    public void jdbcExecuteBatchStart() {
        batchStartNanos = System.nanoTime();
    }

    @Override
    public void jdbcExecuteBatchEnd() {
        RequestQueryStats.recordStatement(System.nanoTime() - batchStartNanos);
    }
}

package kr.allcll.backend.support.metrics;

/**
 * 요청 하나가 DB에 쓴 비용 - 발행한 SQL문 수와 그 실행에 걸린 시간을 함께 수집한다.
 */
public class RequestQueryStats {

    private static final RequestQueryStats EMPTY = new RequestQueryStats();
    private static final ThreadLocal<RequestQueryStats> CURRENT = new ThreadLocal<>();

    private int statementCount;
    private long executionNanos;

    public static void start() {
        CURRENT.set(new RequestQueryStats());
    }

    public static RequestQueryStats stop() {
        RequestQueryStats current = CURRENT.get();
        CURRENT.remove();
        if (current == null) {
            return EMPTY;
        }
        return current;
    }

    static void recordStatement(long elapsedNanos) {
        RequestQueryStats current = CURRENT.get();
        if (current == null) {
            return;
        }
        current.statementCount++;
        current.executionNanos += elapsedNanos;
    }

    public int statementCount() {
        return statementCount;
    }

    public long executionNanos() {
        return executionNanos;
    }
}

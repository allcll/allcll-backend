package kr.allcll.backend.support.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import kr.allcll.backend.support.metrics.SeatPipelineMetrics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AbstractBatchMetricsTest {

    @Test
    @DisplayName("batch flush 메트릭은 첫 flush 전부터 0으로 등록된다.")
    void initializesBatchFlushMetrics() {
        // given
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

        // when
        new TestBatch(new SeatPipelineMetrics(meterRegistry), "general");

        // then
        assertThat(meterRegistry.get("seat.batch.flush.failure.count")
            .tag("type", "general")
            .counter()
            .count()).isZero();
        assertThat(meterRegistry.get("seat.batch.flush.duration")
            .tag("type", "general")
            .timer()
            .count()).isZero();
    }

    @Test
    @DisplayName("batch queue size gauge는 현재 큐 크기를 반영한다.")
    void batchQueueSizeGauge() {
        // given
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        TestBatch batch = new TestBatch(new SeatPipelineMetrics(meterRegistry), "general");

        // when
        batch.add("seat");

        // then
        double queueSize = meterRegistry.get("seat.batch.queue.size")
            .tag("type", "general")
            .gauge()
            .value();
        assertThat(queueSize).isEqualTo(1.0);
    }

    @Test
    @DisplayName("batch에 대기 항목이 있으면 가장 오래 대기한 시간 gauge가 증가하고 flush 후 초기화된다.")
    void batchOldestPendingAgeGauge() {
        // given
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        TestBatch batch = new TestBatch(new SeatPipelineMetrics(meterRegistry), "general");

        // when
        batch.add("seat");

        // then
        await()
            .atMost(Duration.ofSeconds(2))
            .untilAsserted(() -> assertThat(meterRegistry.get("seat.batch.oldest.pending.age")
                .tag("type", "general")
                .gauge()
                .value()).isGreaterThanOrEqualTo(1.0));

        // when
        batch.flush();

        // then
        assertThat(meterRegistry.get("seat.batch.oldest.pending.age")
            .tag("type", "general")
            .gauge()
            .value()).isZero();
    }

    @Test
    @DisplayName("flush 성공 시 duration timer가 기록된다.")
    void batchFlushDuration() {
        // given
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        TestBatch batch = new TestBatch(new SeatPipelineMetrics(meterRegistry), "pin");
        batch.add("seat");

        // when
        batch.flush();

        // then
        assertThat(batch.savedItems).containsExactly("seat");
        assertThat(meterRegistry.get("seat.batch.flush.duration")
            .tag("type", "pin")
            .timer()
            .count()).isEqualTo(1);
    }

    @Test
    @DisplayName("flush 실패 시 failure counter가 증가한다.")
    void batchFlushFailureCount() {
        // given
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        TestBatch batch = new TestBatch(new SeatPipelineMetrics(meterRegistry), "general");
        batch.add("seat");
        batch.failOnSave = true;

        // when & then
        assertThatThrownBy(batch::flush)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("flush failed");
        assertThat(meterRegistry.get("seat.batch.flush.failure.count")
            .tag("type", "general")
            .counter()
            .count()).isEqualTo(1.0);
    }

    private static class TestBatch extends AbstractBatch<String> {

        private final List<String> savedItems = new ArrayList<>();
        private boolean failOnSave;

        private TestBatch(SeatPipelineMetrics seatPipelineMetrics, String type) {
            super(seatPipelineMetrics, type);
        }

        @Override
        protected int getFlushLimit() {
            return 10;
        }

        @Override
        protected void saveAll(List<String> batch) {
            if (failOnSave) {
                throw new IllegalStateException("flush failed");
            }
            savedItems.addAll(batch);
        }
    }
}

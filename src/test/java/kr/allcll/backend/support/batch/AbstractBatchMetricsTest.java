package kr.allcll.backend.support.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.ArrayList;
import java.util.List;
import kr.allcll.backend.support.metrics.SeatPipelineMetrics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AbstractBatchMetricsTest {

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

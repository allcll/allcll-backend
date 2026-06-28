package kr.allcll.backend.support.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SeatPipelineMetricsTest {

    @Test
    @DisplayName("크롤링 성공 시 last crawled age gauge가 갱신된다.")
    void lastCrawledAgeGauge() {
        // given
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        SeatPipelineMetrics seatPipelineMetrics = new SeatPipelineMetrics(meterRegistry);

        // when
        seatPipelineMetrics.recordCrawlingSuccess(System.currentTimeMillis());

        // then
        double ageSeconds = meterRegistry.get("seat.last.crawled.age")
            .gauge()
            .value();
        assertThat(ageSeconds).isBetween(0.0, 1.0);
    }

    @Test
    @DisplayName("스케줄러 작업 성공 시 마지막 성공 시각 gauge가 기록된다.")
    void schedulerLastSuccessTimestampGauge() {
        // given
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        SeatPipelineMetrics seatPipelineMetrics = new SeatPipelineMetrics(meterRegistry);

        // when
        seatPipelineMetrics.recordSchedulerTaskSuccess("general-seat");

        // then
        double timestamp = meterRegistry.get("scheduler.task.last.success.timestamp")
            .tag("task", "general-seat")
            .gauge()
            .value();
        assertThat(timestamp).isPositive();
    }
}

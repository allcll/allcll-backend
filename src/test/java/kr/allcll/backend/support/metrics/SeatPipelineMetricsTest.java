package kr.allcll.backend.support.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SeatPipelineMetricsTest {

    private static final List<String> SSE_EVENT_NAMES = List.of(
        "connection",
        "nonMajorSeats",
        "pinSeats"
    );

    @Test
    @DisplayName("SSE 메트릭은 첫 이벤트 전부터 0으로 등록된다.")
    void initializesSseMetrics() {
        // given
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

        // when
        new SeatPipelineMetrics(meterRegistry);

        // then
        assertThat(meterRegistry.get("sse.send.failure.count").counter().count()).isZero();
        assertThat(meterRegistry.get("sse.send.duration").timer().count()).isZero();
        SSE_EVENT_NAMES.forEach(eventName -> assertThat(meterRegistry.get("sse.event.coalesced")
            .tag("event", eventName)
            .counter()
            .count()).isZero());
    }

    @Test
    @DisplayName("크롤러 시작과 중지에 따라 active gauge가 갱신된다.")
    void seatCrawlerActiveGauge() {
        // given
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        SeatPipelineMetrics seatPipelineMetrics = new SeatPipelineMetrics(meterRegistry);

        // when
        seatPipelineMetrics.recordSeatCrawlerStarted();

        // then
        assertThat(meterRegistry.get("seat.crawler.active").gauge().value()).isEqualTo(1.0);

        // when
        seatPipelineMetrics.recordSeatCrawlerStopped();

        // then
        assertThat(meterRegistry.get("seat.crawler.active").gauge().value()).isZero();
    }

    @Test
    @DisplayName("SSE 스케줄러 시작과 중지에 따라 active gauge가 갱신된다.")
    void seatSseSchedulerActiveGauge() {
        // given
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        SeatPipelineMetrics seatPipelineMetrics = new SeatPipelineMetrics(meterRegistry);

        // when
        seatPipelineMetrics.recordSeatSseSchedulerStarted();

        // then
        assertThat(meterRegistry.get("seat.sse.scheduler.active").gauge().value()).isEqualTo(1.0);

        // when
        seatPipelineMetrics.recordSeatSseSchedulerStopped();

        // then
        assertThat(meterRegistry.get("seat.sse.scheduler.active").gauge().value()).isZero();
    }

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

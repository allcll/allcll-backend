package kr.allcll.backend.support.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import kr.allcll.backend.support.metrics.SeatPipelineMetrics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ScheduledTaskHandlerTest {

    @Test
    @DisplayName("스케줄링 작업을 등록한다.")
    void scheduledTask() {
        // given
        int poolSize = 1;
        ScheduledTaskHandler scheduledTaskHandler = new ScheduledTaskHandler(
            poolSize,
            "test-task",
            new LoggingMeterRegistry()
        );

        // when
        AtomicInteger counter = new AtomicInteger();
        scheduleTask(scheduledTaskHandler, counter);

        // then
        assertThat(scheduledTaskHandler.getTaskCount()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("10개의 작업을 스케줄링에 등록한다.")
    void scheduleManyTask() {
        // given
        int taskCount = 10;
        int poolSize = 10;
        ScheduledTaskHandler scheduledTaskHandler = new ScheduledTaskHandler(
            poolSize,
            "test-task",
            new LoggingMeterRegistry()
        );

        // when
        AtomicInteger counter = new AtomicInteger();
        for (int i = 0; i < taskCount; i++) {
            scheduleTask(scheduledTaskHandler, counter);
        }

        // then
        assertThat(scheduledTaskHandler.getTaskCount()).isGreaterThanOrEqualTo(taskCount);
    }

    @Test
    @DisplayName("스케줄링을 취소하면 작업이 중지된다.")
    void cancel1() {
        // given
        int poolSize = 1;
        ScheduledTaskHandler scheduledTaskHandler = new ScheduledTaskHandler(
            poolSize,
            "test-task",
            new LoggingMeterRegistry()
        );

        // when
        AtomicInteger counter = new AtomicInteger();
        String taskId = scheduleTask(scheduledTaskHandler, counter);

        assertThat(scheduledTaskHandler.getTaskCount()).isEqualTo(1);
        assertThat(scheduledTaskHandler.isRunning(taskId)).isTrue();

        scheduledTaskHandler.cancel(taskId);

        int previousCount = counter.get();

        // then
        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(scheduledTaskHandler.getTaskCount()).isEqualTo(0);
                assertThat(scheduledTaskHandler.isRunning(taskId)).isFalse();
            });

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertThat(counter.get()).isGreaterThanOrEqualTo(previousCount);
    }

    @Test
    @DisplayName("2개의 스케줄링 작업 중 1개를 취소한다.")
    void cancel() {
        // given
        int poolSize = 1;
        ScheduledTaskHandler scheduledTaskHandler = new ScheduledTaskHandler(
            poolSize,
            "test-task",
            new LoggingMeterRegistry()
        );

        // when
        AtomicInteger counter = new AtomicInteger();
        String taskId1 = scheduleTask(scheduledTaskHandler, counter);
        String taskId2 = scheduleTask(scheduledTaskHandler, counter);

        assertThat(scheduledTaskHandler.getTaskCount()).isEqualTo(2);

        scheduledTaskHandler.cancel(taskId2);

        // then
        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(scheduledTaskHandler.getTaskCount()).isEqualTo(1);
                assertThat(scheduledTaskHandler.isRunning(taskId1)).isTrue();
                assertThat(scheduledTaskHandler.isRunning(taskId2)).isFalse();
            });
    }

    @Test
    @DisplayName("모든 작업을 중지한다.")
    void cancelAllScheduler() {
        // given
        int taskCount = 10;
        int poolSize = 10;
        ScheduledTaskHandler scheduledTaskHandler = new ScheduledTaskHandler(
            poolSize,
            "test-task",
            new LoggingMeterRegistry()
        );

        // when
        AtomicInteger counter = new AtomicInteger();
        for (int i = 0; i < taskCount; i++) {
            scheduleTask(scheduledTaskHandler, counter);
        }

        scheduledTaskHandler.cancelAll();

        // then
        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(scheduledTaskHandler.getTaskCount()).isEqualTo(0));
    }

    @Test
    @DisplayName("Task ID가 동일한 작업을 중복 등록할 수 없다.")
    void duplicateTask() {
        ScheduledTaskHandler scheduledTaskHandler = new ScheduledTaskHandler(
            1,
            "test-task",
            new LoggingMeterRegistry()
        );

        // when
        String taskId = scheduleTask(scheduledTaskHandler, new AtomicInteger());
        scheduledTaskHandler.scheduleAtFixedRate(taskId, () -> {
        }, Duration.ofSeconds(1));

        // then
        assertThat(scheduledTaskHandler.getTaskCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("고정 지연 방식으로 스케줄링 작업을 등록한다.")
    void scheduledTaskWithFixedDelay() {
        // given
        ScheduledTaskHandler scheduledTaskHandler = new ScheduledTaskHandler(
            1,
            "test-fixed-delay-task",
            new LoggingMeterRegistry()
        );

        // when
        AtomicInteger counter = new AtomicInteger();
        scheduledTaskHandler.scheduleWithFixedDelay(counter::incrementAndGet, Duration.ofMillis(100));

        // then
        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(scheduledTaskHandler.getTaskCount()).isEqualTo(1);
                assertThat(counter.get()).isGreaterThanOrEqualTo(1);
            });

        scheduledTaskHandler.cancelAll();
    }

    @Test
    @DisplayName("고정 지연 방식 스케줄링 작업도 마지막 성공 시각 gauge를 기록한다.")
    void scheduledTaskWithFixedDelayRecordsLastSuccessTimestamp() {
        // given
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        SeatPipelineMetrics seatPipelineMetrics = new SeatPipelineMetrics(meterRegistry);
        ScheduledTaskHandler scheduledTaskHandler = new ScheduledTaskHandler(
            1,
            "general-seat-sender",
            meterRegistry,
            seatPipelineMetrics
        );

        // when
        AtomicInteger counter = new AtomicInteger();
        scheduledTaskHandler.scheduleWithFixedDelay(counter::incrementAndGet, Duration.ofMillis(100));

        // then
        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                double lastSuccessTimestamp = meterRegistry.get("scheduler.task.last.success.timestamp")
                    .tag("task", "general-seat")
                    .gauge()
                    .value();

                assertThat(counter.get()).isGreaterThanOrEqualTo(1);
                assertThat(lastSuccessTimestamp).isPositive();
            });

        scheduledTaskHandler.cancelAll();
    }

    private String scheduleTask(ScheduledTaskHandler scheduledTaskHandler, AtomicInteger counter) {
        return scheduledTaskHandler.scheduleAtFixedRate(
            () -> System.out.println("Task is running: " + counter.incrementAndGet()),
            Duration.ofSeconds(1)
        );
    }
}

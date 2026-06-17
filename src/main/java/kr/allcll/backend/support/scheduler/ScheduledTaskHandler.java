package kr.allcll.backend.support.scheduler;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import kr.allcll.backend.support.metrics.SeatPipelineMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Slf4j
public class ScheduledTaskHandler {

    private final ThreadPoolTaskScheduler scheduler;
    private final Map<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();
    private final SeatPipelineMetrics seatPipelineMetrics;
    private final String taskName;

    public ScheduledTaskHandler(int poolSize, String namePrefix, MeterRegistry meterRegistry) {
        this(poolSize, namePrefix, meterRegistry, null);
    }

    public ScheduledTaskHandler(
        int poolSize,
        String namePrefix,
        MeterRegistry meterRegistry,
        SeatPipelineMetrics seatPipelineMetrics
    ) {
        scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(poolSize);
        scheduler.setThreadNamePrefix(namePrefix);
        scheduler.setRemoveOnCancelPolicy(true);
        scheduler.initialize();
        this.seatPipelineMetrics = seatPipelineMetrics;
        this.taskName = resolveTaskName(namePrefix);

        ExecutorServiceMetrics.monitor(meterRegistry, scheduler.getScheduledExecutor(), namePrefix);
    }

    public String scheduleAtFixedRate(Runnable task, Duration period) {
        String taskId = UUID.randomUUID().toString();
        return scheduleAtFixedRate(taskId, task, period);
    }

    public String scheduleAtFixedRate(String taskId, Runnable task, Duration period) {
        if (tasks.containsKey(taskId)) {
            log.warn("[ScheduledTaskHandler] Task ID {} 은 이미 스케줄러에 등록되어 있습니다.", taskId);
            return taskId;
        }
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> runWithMetrics(task), period);
        tasks.put(taskId, future);
        return taskId;
    }

    public String scheduleWithFixedDelay(Runnable task, Duration delay) {
        String taskId = UUID.randomUUID().toString();
        return scheduleWithFixedDelay(taskId, task, delay);
    }

    public String scheduleWithFixedDelay(String taskId, Runnable task, Duration delay) {
        if (tasks.containsKey(taskId)) {
            log.warn("[ScheduledTaskHandler] Task ID {} 은 이미 스케줄러에 등록되어 있습니다.", taskId);
            return taskId;
        }
        ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(task, delay);
        tasks.put(taskId, future);
        return taskId;
    }

    public void cancel(String taskId) {
        ScheduledFuture<?> future = tasks.remove(taskId);
        if (future != null) {
            future.cancel(true);
        }
    }

    public void cancelAll() {
        tasks.values().forEach(f -> f.cancel(true));
        tasks.clear();
    }

    public boolean isRunning(String taskId) {
        return tasks.containsKey(taskId);
    }

    public int getTaskCount() {
        return tasks.size();
    }

    private void runWithMetrics(Runnable task) {
        task.run();
        if (seatPipelineMetrics != null) {
            seatPipelineMetrics.recordSchedulerTaskSuccess(taskName);
        }
    }

    private String resolveTaskName(String namePrefix) {
        if ("general-seat-sender".equals(namePrefix)) {
            return "general-seat";
        }
        if ("pin-seat-sender".equals(namePrefix)) {
            return "pin-seat";
        }
        return namePrefix;
    }
}

package kr.allcll.backend.support.schedule;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Slf4j
public class ScheduledTaskHandler {

    private final ThreadPoolTaskScheduler scheduler;
    private final Map<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();
    private final Map<String, AtomicBoolean> statuses = new ConcurrentHashMap<>();

    public ScheduledTaskHandler(int poolSize, String namePrefix, MeterRegistry meterRegistry) {
        scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(poolSize);
        scheduler.setThreadNamePrefix(namePrefix);
        scheduler.initialize();
        scheduler.setRemoveOnCancelPolicy(true);

        ExecutorServiceMetrics.monitor(meterRegistry, scheduler.getScheduledExecutor(), namePrefix);
    }

    public String scheduleAtFixedRate(Runnable task, Duration period) {
        String taskId = UUID.randomUUID().toString();
        AtomicBoolean running = new AtomicBoolean(true);

        Runnable wrappedTask = () -> {
            if (running.get()) {
                task.run();
            }
        };

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(wrappedTask, period);

        tasks.put(taskId, future);
        statuses.put(taskId, running);
        return taskId;
    }

    public void cancel(String taskId) {
        ScheduledFuture<?> future = tasks.remove(taskId);
        if (future != null) {
            future.cancel(true);
        }
        statuses.remove(taskId);
    }

    public void cancelAll() {
        tasks.values().forEach(f -> f.cancel(true));
        tasks.clear();
        statuses.clear();
    }

    public boolean isRunning(String taskId) {
        AtomicBoolean running = statuses.get(taskId);
        return running != null && running.get();
    }

    public int getTaskCount() {
        return tasks.size();
    }
}

package kr.allcll.backend.support.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class SeatPipelineMetrics {

    private static final String TYPE_TAG = "type";
    private static final String TASK_TAG = "task";
    private static final String POOL_TAG = "pool";
    private static final String EVENT_TAG = "event";
    private static final List<String> SSE_EVENT_NAMES = List.of(
        "connection",
        "nonMajorSeats",
        "pinSeats"
    );

    private final MeterRegistry meterRegistry;
    private final AtomicLong seatCrawlerActive = new AtomicLong(0);
    private final AtomicLong seatSseSchedulerActive = new AtomicLong(0);
    private final AtomicLong lastCrawledAtMillis = new AtomicLong(0);
    private final Map<String, Counter> batchFlushFailureCounters = new ConcurrentHashMap<>();
    private final Map<String, Timer> batchFlushDurationTimers = new ConcurrentHashMap<>();
    private final Map<String, Counter> sseEventCoalescedCounters = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> schedulerLastSuccessEpochSeconds = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> schedulerPoolMaxActiveThreads = new ConcurrentHashMap<>();
    private final Map<String, Timer> schedulerTaskDurationTimers = new ConcurrentHashMap<>();
    private final Counter sseSendFailureCounter;
    private final Timer sseSendDurationTimer;

    public SeatPipelineMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.sseSendFailureCounter = Counter.builder("sse.send.failure.count")
            .register(meterRegistry);
        this.sseSendDurationTimer = Timer.builder("sse.send.duration")
            .publishPercentileHistogram()
            .register(meterRegistry);
        SSE_EVENT_NAMES.forEach(eventName -> sseEventCoalescedCounters.computeIfAbsent(
            eventName,
            this::registerSseEventCoalescedCounter
        ));
        Gauge.builder("seat.crawler.active", seatCrawlerActive, AtomicLong::get)
            .register(meterRegistry);
        Gauge.builder("seat.sse.scheduler.active", seatSseSchedulerActive, AtomicLong::get)
            .register(meterRegistry);
        Gauge.builder("seat.last.crawled.age", lastCrawledAtMillis, this::getLastCrawledAgeSeconds)
            .baseUnit("seconds")
            .register(meterRegistry);
    }

    public void recordSeatCrawlerStarted() {
        seatCrawlerActive.set(1);
    }

    public void recordSeatCrawlerStopped() {
        seatCrawlerActive.set(0);
    }

    public void recordSeatSseSchedulerStarted() {
        seatSseSchedulerActive.set(1);
    }

    public void recordSeatSseSchedulerStopped() {
        seatSseSchedulerActive.set(0);
    }

    public void recordCrawlingSuccess(long epochMillis) {
        lastCrawledAtMillis.updateAndGet(previous -> Math.max(previous, epochMillis));
    }

    public void registerBatchQueueSize(String type, BlockingQueue<?> queue) {
        Gauge.builder("seat.batch.queue.size", queue, BlockingQueue::size)
            .tags(TYPE_TAG, type)
            .register(meterRegistry);
    }

    public void registerBatchOldestPendingAge(String type, AtomicLong oldestPendingAtMillis) {
        Gauge.builder("seat.batch.oldest.pending.age", oldestPendingAtMillis, this::getPendingAgeSeconds)
            .tags(TYPE_TAG, type)
            .baseUnit("seconds")
            .register(meterRegistry);
    }

    public void registerBatchFlushMetrics(String type) {
        batchFlushFailureCounters.computeIfAbsent(type, this::registerBatchFlushFailureCounter);
        batchFlushDurationTimers.computeIfAbsent(type, this::registerBatchFlushDurationTimer);
    }

    public void recordBatchFlush(String type, ThrowingRunnable runnable) {
        Counter failureCounter = batchFlushFailureCounters.computeIfAbsent(
            type,
            this::registerBatchFlushFailureCounter
        );
        Timer durationTimer = batchFlushDurationTimers.computeIfAbsent(type, this::registerBatchFlushDurationTimer);
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            runnable.run();
        } catch (Exception e) {
            failureCounter.increment();
            throwAsUnchecked(e);
        } finally {
            sample.stop(durationTimer);
        }
    }

    public void recordSseSend(ThrowingRunnable runnable) throws Exception {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            runnable.run();
        } catch (Exception e) {
            sseSendFailureCounter.increment();
            throw e;
        } finally {
            sample.stop(sseSendDurationTimer);
        }
    }

    public void recordSseEventCoalesced(String eventName) {
        sseEventCoalescedCounters.computeIfAbsent(
            eventName,
            this::registerSseEventCoalescedCounter
        ).increment();
    }

    public void registerSchedulerTask(String task) {
        schedulerLastSuccessEpochSeconds.computeIfAbsent(task, this::registerSchedulerGauge);
        registerSchedulerDurationMetrics(task);
    }

    public void recordSchedulerTask(String task, Runnable runnable) {
        registerSchedulerDurationMetrics(task);
        Timer durationTimer = schedulerTaskDurationTimers.get(task);

        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            runnable.run();
        } finally {
            sample.stop(durationTimer);
        }
    }

    public void registerSchedulerPool(String pool) {
        schedulerPoolMaxActiveThreads.computeIfAbsent(pool, this::registerSchedulerPoolMaxActiveGauge);
    }

    public void recordSchedulerPoolActiveThreads(String pool, int activeThreads) {
        schedulerPoolMaxActiveThreads
            .computeIfAbsent(pool, this::registerSchedulerPoolMaxActiveGauge)
            .accumulateAndGet(activeThreads, Math::max);
    }

    public void recordSchedulerTaskSuccess(String task) {
        AtomicLong lastSuccess = schedulerLastSuccessEpochSeconds.computeIfAbsent(task, this::registerSchedulerGauge);
        lastSuccess.set(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
    }

    private AtomicLong registerSchedulerGauge(String task) {
        AtomicLong lastSuccess = new AtomicLong(0);
        Gauge.builder("scheduler.task.last.success.timestamp", lastSuccess, AtomicLong::get)
            .tags(Tags.of(TASK_TAG, task))
            .register(meterRegistry);
        return lastSuccess;
    }

    private void registerSchedulerDurationMetrics(String task) {
        schedulerTaskDurationTimers.computeIfAbsent(task, this::registerSchedulerTaskDurationTimer);
    }

    private AtomicInteger registerSchedulerPoolMaxActiveGauge(String pool) {
        AtomicInteger maxActiveThreads = new AtomicInteger(0);
        Gauge.builder("scheduler.pool.max.active", maxActiveThreads, AtomicInteger::get)
            .tags(Tags.of(POOL_TAG, pool))
            .register(meterRegistry);
        return maxActiveThreads;
    }

    private Timer registerSchedulerTaskDurationTimer(String task) {
        return Timer.builder("scheduler.task.duration")
            .tags(Tags.of(TASK_TAG, task))
            .publishPercentileHistogram()
            .register(meterRegistry);
    }

    private Counter registerBatchFlushFailureCounter(String type) {
        return Counter.builder("seat.batch.flush.failure.count")
            .tags(TYPE_TAG, type)
            .register(meterRegistry);
    }

    private Timer registerBatchFlushDurationTimer(String type) {
        return Timer.builder("seat.batch.flush.duration")
            .tags(TYPE_TAG, type)
            .publishPercentileHistogram()
            .register(meterRegistry);
    }

    private Counter registerSseEventCoalescedCounter(String eventName) {
        return Counter.builder("sse.event.coalesced")
            .tags(EVENT_TAG, eventName)
            .register(meterRegistry);
    }

    private double getLastCrawledAgeSeconds(AtomicLong lastCrawledAtMillis) {
        long lastCrawledAt = lastCrawledAtMillis.get();
        if (lastCrawledAt == 0) {
            return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        }
        return (System.currentTimeMillis() - lastCrawledAt) / 1000.0;
    }

    private double getPendingAgeSeconds(AtomicLong oldestPendingAtMillis) {
        long oldestPendingAt = oldestPendingAtMillis.get();
        if (oldestPendingAt == 0) {
            return 0;
        }
        return (System.currentTimeMillis() - oldestPendingAt) / 1000.0;
    }

    private void throwAsUnchecked(Exception e) {
        if (e instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        throw new IllegalStateException(e);
    }

    @FunctionalInterface
    public interface ThrowingRunnable {

        void run() throws Exception;
    }
}

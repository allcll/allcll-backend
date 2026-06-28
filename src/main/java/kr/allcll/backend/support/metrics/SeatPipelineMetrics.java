package kr.allcll.backend.support.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class SeatPipelineMetrics {

    private static final String TYPE_TAG = "type";
    private static final String TASK_TAG = "task";
    private static final String EVENT_TAG = "event";

    private final MeterRegistry meterRegistry;
    private final AtomicLong lastCrawledAtMillis = new AtomicLong(0);
    private final Map<String, AtomicLong> schedulerLastSuccessEpochSeconds = new ConcurrentHashMap<>();

    public SeatPipelineMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        Gauge.builder("seat.last.crawled.age", lastCrawledAtMillis, this::getLastCrawledAgeSeconds)
            .baseUnit("seconds")
            .register(meterRegistry);
    }

    public void recordCrawlingSuccess(long epochMillis) {
        lastCrawledAtMillis.updateAndGet(previous -> Math.max(previous, epochMillis));
    }

    public void registerBatchQueueSize(String type, BlockingQueue<?> queue) {
        Gauge.builder("seat.batch.queue.size", queue, BlockingQueue::size)
            .tags(TYPE_TAG, type)
            .register(meterRegistry);
    }

    public void recordBatchFlush(String type, ThrowingRunnable runnable) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            runnable.run();
        } catch (Exception e) {
            Counter.builder("seat.batch.flush.failure.count")
                .tags(TYPE_TAG, type)
                .register(meterRegistry)
                .increment();
            throwAsUnchecked(e);
        } finally {
            sample.stop(Timer.builder("seat.batch.flush.duration")
                .tags(TYPE_TAG, type)
                .publishPercentileHistogram()
                .register(meterRegistry));
        }
    }

    public void recordSseSend(ThrowingRunnable runnable) throws Exception {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            runnable.run();
        } catch (Exception e) {
            Counter.builder("sse.send.failure.count")
                .register(meterRegistry)
                .increment();
            throw e;
        } finally {
            sample.stop(Timer.builder("sse.send.duration")
                .publishPercentileHistogram()
                .register(meterRegistry));
        }
    }

    public void recordSseEventCoalesced(String eventName) {
        Counter.builder("sse.event.coalesced")
            .tags(EVENT_TAG, eventName)
            .register(meterRegistry)
            .increment();
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

    private double getLastCrawledAgeSeconds(AtomicLong lastCrawledAtMillis) {
        long lastCrawledAt = lastCrawledAtMillis.get();
        if (lastCrawledAt == 0) {
            return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        }
        return (System.currentTimeMillis() - lastCrawledAt) / 1000.0;
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

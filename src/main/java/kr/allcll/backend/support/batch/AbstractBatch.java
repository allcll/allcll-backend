package kr.allcll.backend.support.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import kr.allcll.backend.support.metrics.SeatPipelineMetrics;

public abstract class AbstractBatch<T> {

    private final BlockingQueue<T> queue;
    private final Object lock;
    private final SeatPipelineMetrics seatPipelineMetrics;
    private final String type;

    protected abstract int getFlushLimit();

    protected abstract void saveAll(List<T> batch);

    public AbstractBatch(SeatPipelineMetrics seatPipelineMetrics, String type) {
        this.queue = new LinkedBlockingQueue<>();
        this.lock = new Object();
        this.seatPipelineMetrics = seatPipelineMetrics;
        this.type = type;
        this.seatPipelineMetrics.registerBatchQueueSize(type, queue::size);
    }

    public void add(T item) {
        queue.add(item);
        if (queue.size() >= getFlushLimit()) {
            flush();
        }
    }

    private List<T> getAll() {
        List<T> batch = new ArrayList<>();
        queue.drainTo(batch);
        return batch;
    }

    public void flush() {
        List<T> batch;
        synchronized (lock) {
            batch = getAll();
            if (batch.isEmpty()) {
                return;
            }
        }
        seatPipelineMetrics.recordBatchFlush(type, () -> saveAll(batch));
    }
}

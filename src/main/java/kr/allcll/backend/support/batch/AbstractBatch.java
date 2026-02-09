package kr.allcll.backend.support.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class AbstractBatch<T> {

    private final BlockingQueue<T> queue;
    private final Object lock;

    protected abstract int getFlushLimit();

    protected abstract void saveAll(List<T> batch);

    public AbstractBatch() {
        this.queue = new LinkedBlockingQueue<>();
        this.lock = new Object();
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
        saveAll(batch);
    }
}

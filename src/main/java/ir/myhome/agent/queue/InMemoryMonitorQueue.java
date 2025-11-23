package ir.myhome.agent.queue;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryMonitorQueue<T> implements MonitorQueue<T> {

    private final LinkedBlockingQueue<T> queue;
    private final AtomicLong processed = new AtomicLong(0);
    private final AtomicLong dropped = new AtomicLong(0);
    private volatile boolean shutdown = false;

    public InMemoryMonitorQueue(int capacity) {
        this.queue = new LinkedBlockingQueue<>(capacity);
    }

    @Override
    public boolean enqueue(T item, BackpressureStrategy strategy, long timeoutMillis) {
        if (shutdown) {
            dropped.incrementAndGet();
            return false;
        }
        try {
            switch (strategy) {
                case BLOCK:
                    queue.put(item);
                    return true;
                case WAIT_WITH_TIMEOUT:
                    boolean ok = queue.offer(item, timeoutMillis, TimeUnit.MILLISECONDS);
                    if (!ok) dropped.incrementAndGet();
                    return ok;
                case DROP:
                default:
                    boolean offered = queue.offer(item);
                    if (!offered) dropped.incrementAndGet();
                    return offered;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            dropped.incrementAndGet();
            return false;
        }
    }

    @Override
    public T take() throws InterruptedException {
        T item = queue.take();
        processed.incrementAndGet();
        return item;
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public void shutdown() {
        shutdown = true;
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    public long getProcessedCount() { return processed.get(); }
    public long getDroppedCount() { return dropped.get(); }
}

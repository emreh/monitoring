package ir.myhome.agent.queue;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class BoundedSpanQueue<T> implements SpanQueue<T> {

    private final ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<>();
    private final AtomicInteger size = new AtomicInteger(0);
    private final AtomicLong dropped = new AtomicLong(0);
    private final int capacity;

    public BoundedSpanQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be > 0");
        }
        this.capacity = capacity;
    }

    @Override
    public boolean offer(T item) {
        int current;
        do {
            current = size.get();
            if (current >= capacity) {
                dropped.incrementAndGet();
                return false; // invariant #2
            }
        } while (!size.compareAndSet(current, current + 1));

        queue.offer(item);
        return true;
    }

    @Override
    public T poll() {
        T item = queue.poll();
        if (item != null) {
            size.decrementAndGet();
        }
        return item;
    }

    @Override
    public int size() {
        return size.get();
    }

    @Override
    public long dropped() {
        return dropped.get();
    }

    @Override
    public int capacity() {
        return capacity;
    }
}

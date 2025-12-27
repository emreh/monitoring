package ir.myhome.agent.queue;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public final class SpanQueueImpl<T> implements SpanQueue<T> {

    private final ArrayBlockingQueue<T> queue;
    private final AtomicLong dropped = new AtomicLong();

    public SpanQueueImpl(int capacity) {
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    @Override
    public boolean offer(T item) {
        boolean success = queue.offer(item);

        if (!success) dropped.incrementAndGet();

        return success;
    }

    @Override
    public T poll() {
        return queue.poll();
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public long dropped() {
        return dropped.get();
    }

    @Override
    public int capacity() {
        return queue.remainingCapacity() + queue.size();
    }

    @Override
    public int drainTo(List<T> dst, int maxElements) {
        int drained = 0;
        for (int i = 0; i < maxElements; i++) {
            T item = queue.poll();
            if (item == null) break;
            dst.add(item);
            drained++;
        }
        return drained;
    }
}

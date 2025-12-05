package ir.myhome.agent.queue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public final class BoundedSpanQueue implements SpanQueue {

    private final ArrayBlockingQueue<Object> q;

    public BoundedSpanQueue(int capacity) {
        this.q = new ArrayBlockingQueue<>(Math.max(1, capacity));
    }

    @Override
    public boolean offer(Object span) {
        try {
            return q.offer(span, 2, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public Object poll() {
        return q.poll();
    }

    @Override
    public Object take() throws InterruptedException {
        return q.take();
    }

    @Override
    public int size() {
        return q.size();
    }
}

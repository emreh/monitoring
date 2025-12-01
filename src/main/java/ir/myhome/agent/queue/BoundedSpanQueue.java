package ir.myhome.agent.queue;

import ir.myhome.agent.core.Span;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public final class BoundedSpanQueue implements SpanQueue {

    private final ConcurrentLinkedQueue<Span> q = new ConcurrentLinkedQueue<>();
    private final AtomicInteger size = new AtomicInteger(0);
    private final int capacity;

    public BoundedSpanQueue(int capacity) {
        this.capacity = Math.max(1, capacity);
    }

    @Override
    public boolean offer(Span span) {
        if (span == null) return false;

        while (true) {
            int s = size.get();

            if (s >= capacity) {
                Span polled = q.poll();

                if (polled != null) size.decrementAndGet();
                else return false;
            } else {
                if (size.compareAndSet(s, s + 1)) {
                    q.add(span);
                    return true;
                }
            }
        }
    }

    @Override
    public int drainTo(Span[] buffer, int maxItems) {
        int i = 0;

        while (i < maxItems) {
            Span p = q.poll();

            if (p == null) break;
            buffer[i++] = p;
            size.decrementAndGet();
        }
        return i;
    }

    @Override
    public int size() {
        return size.get();
    }
}

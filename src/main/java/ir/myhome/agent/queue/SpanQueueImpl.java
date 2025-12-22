package ir.myhome.agent.queue;

import ir.myhome.agent.core.Span;
import ir.myhome.agent.holder.AgentHolder;
import ir.myhome.agent.policy.contract.*;
import ir.myhome.agent.util.PolicyUtils;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class SpanQueueImpl<T> implements SpanQueue<T> {

    private final ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<>();
    private final AtomicInteger size = new AtomicInteger(0);
    private final AtomicLong dropped = new AtomicLong(0);
    private final int capacity;

    public SpanQueueImpl(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        this.capacity = capacity;
    }

    @Override
    public boolean offer(T item) {

        // ---------- Stage 11: Policy Gate ----------
        if (item instanceof Span span) {
            PolicyEngine policy = AgentHolder.getPolicyEngine();
            if (policy != null) {
                PolicyInput input = new PolicyInput(
                        PolicyUtils.fastHash64(span.traceId),
                        OverloadState.NORMAL
                );
                Decision decision = policy.evaluate(input);
                if (decision.type() != DecisionType.ALLOW) {
                    dropped.incrementAndGet();
                    return false;
                }
            }
        }
        // ------------------------------------------

        int current;
        do {
            current = size.get();
            if (current >= capacity) {
                dropped.incrementAndGet();
                return false;
            }
        } while (!size.compareAndSet(current, current + 1));

        queue.offer(item);
        return true;
    }

    @Override
    public T poll() {
        T item = queue.poll();
        if (item != null) {
            int prev;
            do {
                prev = size.get();
            } while (!size.compareAndSet(prev, prev - 1));
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

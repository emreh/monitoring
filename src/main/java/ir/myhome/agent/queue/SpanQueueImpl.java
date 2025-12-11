package ir.myhome.agent.queue;

import ir.myhome.agent.core.Span;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SpanQueueImpl implements SpanQueue {

    private final BlockingQueue<Span> queue;

    public SpanQueueImpl(int capacity) {
        this.queue = new LinkedBlockingQueue<>(capacity);
    }

    @Override
    public boolean offer(Object span) {
        return queue.offer((Span) span);
    }

    @Override
    public Span poll() {
        return queue.poll();
    }

    @Override
    public Span take() throws InterruptedException {
        return queue.take();
    }

    @Override
    public int size() {
        return queue.size();
    }

    public Span poll(long timeoutMs) throws InterruptedException {
        return queue.poll(timeoutMs, TimeUnit.MILLISECONDS);
    }
}

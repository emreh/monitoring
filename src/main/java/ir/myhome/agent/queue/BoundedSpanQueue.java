package ir.myhome.agent.queue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BoundedSpanQueue implements SpanQueue {

    private final BlockingQueue<Object> queue;

    public BoundedSpanQueue(int capacity) {
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    @Override
    public boolean offer(Object span) {
        return queue.offer(span);
    }

    @Override
    public Object poll() {
        return queue.poll();
    }

    @Override
    public Object take() throws InterruptedException {
        return queue.take();
    }

    @Override
    public int size() {
        return queue.size();
    }
}

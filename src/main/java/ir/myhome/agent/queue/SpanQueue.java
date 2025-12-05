package ir.myhome.agent.queue;

public interface SpanQueue {

    boolean offer(Object span);

    Object poll();

    Object take() throws InterruptedException;

    int size();
}

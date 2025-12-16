package ir.myhome.agent.queue;

public interface SpanQueue<T> {

    boolean offer(T item);     // non-blocking

    T poll();                  // non-blocking

    int size();

    long dropped();

    int capacity();
}

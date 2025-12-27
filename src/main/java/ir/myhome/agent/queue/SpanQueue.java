package ir.myhome.agent.queue;

import java.util.List;

public interface SpanQueue<T> {

    boolean offer(T item);     // non-blocking

    T poll();                  // non-blocking

    int size();

    long dropped();

    int capacity();

    int drainTo(List<T> dst, int maxElements);
}

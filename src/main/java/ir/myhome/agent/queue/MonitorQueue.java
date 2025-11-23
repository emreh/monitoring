package ir.myhome.agent.queue;

public interface MonitorQueue<T> {

    enum BackpressureStrategy {
        DROP,
        BLOCK,
        WAIT_WITH_TIMEOUT
    }

    boolean enqueue(T item, BackpressureStrategy strategy, long timeoutMillis);

    T take() throws InterruptedException;

    int size();

    void shutdown();

    boolean isShutdown();
}

package ir.myhome.agent.queue;

/**
 * ساده‌ترین wrapper برای producer
 */
public class RecordProducer<T> {

    private final MonitorQueue<T> queue;
    private final MonitorQueue.BackpressureStrategy strategy;
    private final long timeoutMillis;

    public RecordProducer(MonitorQueue<T> queue, MonitorQueue.BackpressureStrategy strategy, long timeoutMillis) {
        this.queue = queue;
        this.strategy = strategy;
        this.timeoutMillis = timeoutMillis;
    }

    public boolean submit(T record) {
        return queue.enqueue(record, strategy, timeoutMillis);
    }
}


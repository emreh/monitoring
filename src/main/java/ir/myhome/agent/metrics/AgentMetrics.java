package ir.myhome.agent.metrics;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class AgentMetrics {

    private final AtomicInteger queueSize = new AtomicInteger(0);
    private final AtomicInteger flushedCount = new AtomicInteger(0);
    private final AtomicLong lastFlushEpoch = new AtomicLong(0);

    public void setQueueSize(int v) {
        queueSize.set(v);
    }

    public int getQueueSize() {
        return queueSize.get();
    }

    public void addFlushed(int n) {
        flushedCount.addAndGet(n);
        lastFlushEpoch.set(System.currentTimeMillis());
    }

    public int getFlushedCount() {
        return flushedCount.get();
    }

    public long getLastFlushEpoch() {
        return lastFlushEpoch.get();
    }
}

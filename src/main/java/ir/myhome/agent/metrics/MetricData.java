package ir.myhome.agent.metrics;

import java.util.concurrent.atomic.AtomicLong;

public final class MetricData {

    private final AtomicLong count = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);

    public void incrementCount() {
        count.incrementAndGet();
    }

    public void incrementError() {
        errorCount.incrementAndGet();
    }

    public long getCount() {
        return count.get();
    }

    public long getErrorCount() {
        return errorCount.get();
    }
}

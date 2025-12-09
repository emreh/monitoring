package ir.myhome.agent.metrics;

import org.HdrHistogram.Histogram;

import java.util.concurrent.atomic.AtomicLong;

public final class MetricData {

    private final AtomicLong count = new AtomicLong();
    private final AtomicLong errorCount = new AtomicLong();
    private final Histogram histogram;

    public MetricData() {
        // maxValue = 60s, precision = 2 digits
        this.histogram = new Histogram(60000, 2);
    }

    public synchronized void recordLatency(long latencyMs) {
        histogram.recordValue(latencyMs);
        count.incrementAndGet();
    }

    public void incrementCount() {
        count.incrementAndGet();
    }

    public void incrementError() {
        errorCount.incrementAndGet();
    }

    public long getPercentile(double percentile) {
        return histogram.getValueAtPercentile(percentile);
    }

    public long getCount() {
        return count.get();
    }

    public long getErrorCount() {
        return errorCount.get();
    }
}

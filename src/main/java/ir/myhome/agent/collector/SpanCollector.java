package ir.myhome.agent.collector;

import com.tdunning.math.stats.TDigest;
import ir.myhome.agent.core.Span;

import java.util.concurrent.atomic.AtomicLong;

public class SpanCollector {

    private final TDigest durationDigest = TDigest.createDigest(100.0);
    private final AtomicLong count = new AtomicLong(0);

    public synchronized void collect(Span span) {
        span.end();
        durationDigest.add(span.durationMs);
        count.incrementAndGet();
    }

    public long getCount() {
        return count.get();
    }

    public double getPercentile(double q) {
        return durationDigest.quantile(q);
    }

    public TDigest snapshotDigest() {
        return durationDigest;
    }
}

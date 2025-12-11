package ir.myhome.agent.core;

import com.tdunning.math.stats.MergingDigest;
import com.tdunning.math.stats.TDigest;

public class MetricsAggregator {

    private final TDigest digest = new MergingDigest(100.0);

    public synchronized void record(Span span) {
        digest.add(span.durationMs);
    }

    public synchronized double percentile(double p) {
        return digest.quantile(p / 100.0);
    }
}

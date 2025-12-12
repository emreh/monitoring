package ir.myhome.agent.collector;

import com.tdunning.math.stats.TDigest;

public final class TDigestCollector implements PercentileCollector {

    private final TDigest digest;

    public TDigestCollector(double compressionRatio) {
        this.digest = TDigest.createDigest(compressionRatio); // compression factor
    }

    @Override
    public synchronized void record(long value) {
        digest.add(value);
    }

    @Override
    public synchronized double percentile(double p) {
        return digest.quantile(p / 100.0);
    }

    @Override
    public synchronized long count() {
        return (long) digest.size();
    }
}

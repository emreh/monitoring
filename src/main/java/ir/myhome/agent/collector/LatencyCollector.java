package ir.myhome.agent.collector;

import com.tdunning.math.stats.TDigest;
import ir.myhome.agent.snapshot.WindowSnapshot;
import org.HdrHistogram.Histogram;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public final class LatencyCollector {

    private final Histogram hdr;
    private final TDigest tDigest;
    private final AtomicLong count = new AtomicLong();
    private final AtomicBoolean sealed = new AtomicBoolean(false);

    public LatencyCollector(long maxLatencyMicros, int significantFigures) {
        this.hdr = new Histogram(maxLatencyMicros, significantFigures);
        this.tDigest = TDigest.createDigest(100);
    }

    public void record(long micros) {
        if (sealed.get()) return;

        synchronized (this) {
            if (sealed.get()) return;
            hdr.recordValue(micros);
            tDigest.add(micros);
            count.incrementAndGet();
        }
    }

    public WindowSnapshot snapshot() {
        if (!sealed.compareAndSet(false, true)) {
            throw new IllegalStateException("Collector already snapshotted");
        }

        synchronized (this) {
            return new WindowSnapshot(
                    count.get(),
                    (long) tDigest.quantile(0.5),
                    (long) tDigest.quantile(0.9),
                    (long) tDigest.quantile(0.99),
                    hdr.getValueAtPercentile(99.0)
            );
        }
    }

    public boolean isSealed() {
        return sealed.get();
    }
}

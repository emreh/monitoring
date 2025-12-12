package ir.myhome.agent.collector;

import org.HdrHistogram.Histogram;

public final class HDRHistogramCollector implements PercentileCollector {

    private final Histogram histogram;

    public HDRHistogramCollector(long lowestDiscernibleValue, long highestTrackableValue, int significantFigures) {
        this.histogram = new Histogram(lowestDiscernibleValue, highestTrackableValue, significantFigures);
    }

    @Override
    public synchronized void record(long value) {
        histogram.recordValue(value);
    }

    @Override
    public synchronized double percentile(double p) {
        return histogram.getValueAtPercentile(p);
    }

    @Override
    public synchronized long count() {
        return histogram.getTotalCount();
    }
}

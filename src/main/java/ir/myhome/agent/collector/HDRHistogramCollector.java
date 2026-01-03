package ir.myhome.agent.collector;

import org.HdrHistogram.Histogram;

/**
 * synchronized حذف شد: دیگر نیازی به استفاده از قفل‌ها برای عملیات روی Histogram نیست.
 */
public final class HDRHistogramCollector implements PercentileCollector {

    private final Histogram histogram;

    public HDRHistogramCollector(long lowestDiscernibleValue, long highestTrackableValue, int significantFigures) {
        this.histogram = new Histogram(lowestDiscernibleValue, highestTrackableValue, significantFigures);
    }

    @Override
    public void record(long value) {
        histogram.recordValue(value);  // ثبت مقدار در histogram به‌صورت thread-safe
    }

    @Override
    public double percentile(double p) {
        return histogram.getValueAtPercentile(p);  // دریافت درصد
    }

    @Override
    public long count() {
        return histogram.getTotalCount();  // تعداد کل رکوردها
    }
}

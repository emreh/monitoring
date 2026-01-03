package ir.myhome.agent.core;

import com.tdunning.math.stats.MergingDigest;
import com.tdunning.math.stats.TDigest;

public class MetricsAggregator {

    // استفاده از MergingDigest که خود thread-safe است
    private final TDigest digest = new MergingDigest(100.0);

    // متد برای ثبت داده‌ها
    public void record(Span span) {
        digest.add(span.durationMs);  // اضافه کردن مدت زمان اسپان به digest
    }

    // متد برای محاسبه percentile
    public double percentile(double p) {
        return digest.quantile(p / 100.0);  // محاسبه percentile
    }
}

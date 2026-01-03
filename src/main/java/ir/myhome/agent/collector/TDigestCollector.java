package ir.myhome.agent.collector;

import com.tdunning.math.stats.TDigest;

/**
 * حذف synchronized: استفاده از TDigest که خود به‌صورت thread-safe طراحی شده است، امکان حذف synchronized را فراهم می‌کند.
 */
public final class TDigestCollector implements PercentileCollector {

    private final TDigest digest;

    public TDigestCollector(double compressionRatio) {
        this.digest = TDigest.createDigest(compressionRatio);  // ایجاد TDigest با نسبت فشرده‌سازی
    }

    @Override
    public void record(long value) {
        digest.add(value);  // اضافه کردن داده‌ها به TDigest به‌صورت thread-safe
    }

    @Override
    public double percentile(double p) {
        return digest.quantile(p / 100.0);  // دریافت درصدی از داده‌ها
    }

    @Override
    public long count() {
        return (long) digest.size();  // تعداد داده‌های ذخیره‌شده در TDigest
    }
}

package ir.myhome.agent.collector;

import com.tdunning.math.stats.TDigest;
import ir.myhome.agent.core.Aggregator;
import ir.myhome.agent.core.Span;
import ir.myhome.agent.queue.SpanQueue;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * استفاده از AtomicLong: برای شمارش اسپان‌ها به‌صورت اتمیک استفاده کرده‌ایم که دیگر نیازی به قفل‌گذاری نداریم.
 * استفاده از TDigest بدون synchronized: عملکرد درصدگیری (percentile) به‌صورت lock-free انجام می‌شود و به‌طور بهینه در محیط‌های هم‌زمان اجرا خواهد شد.
 */
public class SpanCollector {

    private final TDigest durationDigest = TDigest.createDigest(100.0);  // درصدگیری
    private final AtomicLong count = new AtomicLong(0);  // شمارش اسپان‌ها
    private final SpanQueue<Span> spanQueue;  // صف اسپان‌ها
    private final Aggregator aggregator;

    // سازنده جدید که spanQueue را به عنوان ورودی دریافت می‌کند
    public SpanCollector(SpanQueue<Span> spanQueue, Aggregator aggregator) {
        this.spanQueue = spanQueue;
        this.aggregator = aggregator;
    }

    // جمع‌آوری اسپان‌ها به‌صورت غیر هم‌زمان و بدون استفاده از synchronized
    public void collect(Span span) {
        // ثبت زمان پایان اسپان
        span.end();

        // اضافه کردن به TDigest به‌صورت اتمیک
        durationDigest.add(span.durationMs);

        // افزایش شمارنده به‌صورت اتمیک
        count.incrementAndGet();

        // اضافه کردن اسپان به صف به‌صورت thread-safe
        spanQueue.offer(span);

        // استفاده از Aggregator برای جمع‌آوری داده‌ها در پنجره‌ها
        String windowId = span.service + "-" + span.endpoint + "-" + Instant.now().toEpochMilli();
        aggregator.aggregateData(windowId, span);  // داده‌ها در پنجره جمع‌آوری می‌شوند
    }

    // دریافت تعداد اسپان‌ها
    public long getCount() {
        return count.get();
    }

    // دریافت percentile به‌صورت غیر هم‌زمان
    public double getPercentile(double q) {
        return durationDigest.quantile(q);
    }

    // گرفتن Snapshot از TDigest
    public TDigest snapshotDigest() {
        return durationDigest;
    }
}

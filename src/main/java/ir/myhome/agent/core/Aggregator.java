package ir.myhome.agent.core;

import ir.myhome.agent.window.Window;
import ir.myhome.agent.window.WindowState;

import java.util.HashMap;
import java.util.Map;

public class Aggregator {

    private final Map<String, Window> windows = new HashMap<>();
    private final MetricsAggregator metricsAggregator;  // استفاده از MetricsAggregator برای پردازش متریک‌ها

    // سازنده برای اتصال Aggregator به MetricsAggregator
    public Aggregator(MetricsAggregator metricsAggregator) {
        this.metricsAggregator = metricsAggregator;
    }

    // متد برای افزودن داده‌ها به پنجره‌های زمانی
    public void aggregateData(String windowId, Span span) {
        // بررسی و یا ایجاد پنجره زمانی برای windowId
        Window window = windows.computeIfAbsent(windowId, id -> new Window());

        if (window.state() == WindowState.OPEN) {
            // افزودن داده‌ها به پنجره باز
            System.out.println("Aggregating data for window: " + windowId);
            // استفاده از MetricsAggregator برای جمع‌آوری داده‌ها
            metricsAggregator.record(span);  // استفاده از کلاس MetricsAggregator برای ثبت متریک‌ها
        } else if (window.state() == WindowState.SNAPSHOT_TAKEN) {
            // اگر snapshot گرفته شده باشد، داده‌ها باید در یک پنجره جدید جمع شوند
            System.out.println("Snapshot taken, aggregating in new window for: " + windowId);
            // انجام عملیات مشابه برای پنجره جدید
            metricsAggregator.record(span);  // استفاده از MetricsAggregator برای ثبت متریک‌ها
        }
    }

    // متد برای گرفتن snapshot از پنجره
    public void takeSnapshot(String windowId) {
        Window window = windows.get(windowId);
        if (window != null) {
            window.snapshot();
            System.out.println("Snapshot taken for window: " + windowId);
        }
    }

    // متد برای بستن پنجره
    public void closeWindow(String windowId) {
        Window window = windows.get(windowId);
        if (window != null) {
            window.close();
            System.out.println("Window closed: " + windowId);
        }
    }

    // متد برای دریافت Percentile از MetricsAggregator
    public double getPercentile(double p) {
        return metricsAggregator.percentile(p);  // استفاده از MetricsAggregator برای دریافت percentile
    }
}

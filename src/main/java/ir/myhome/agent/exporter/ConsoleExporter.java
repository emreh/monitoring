package ir.myhome.agent.exporter;

import ir.myhome.agent.metrics.MetricCollectorSingleton;
import ir.myhome.agent.metrics.MetricSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public final class ConsoleExporter implements Exporter, Runnable {

    private final BlockingQueue<MetricSnapshot> queue;

    public ConsoleExporter() {
        this.queue = MetricCollectorSingleton.get().getExportQueue();
    }

    @Override
    public void export(Map<String, Object> span) {
        try {
            // تبدیل Map به MetricSnapshot برای صف export
            MetricSnapshot snapshot = new MetricSnapshot(
                    (String) span.get("metricName"),
                    (Long) span.get("count"),
                    (Long) span.get("errorCount"),
                    (Long) span.get("p50"),
                    (Long) span.get("p90"),
                    (Long) span.get("p99")
            );
            queue.put(snapshot);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                MetricSnapshot snapshot = queue.take();
                Map<String, Object> map = new HashMap<>();
                map.put("metricName", snapshot.metricName());
                map.put("count", snapshot.count());
                map.put("errorCount", snapshot.errorCount());
                map.put("p50", snapshot.p50());
                map.put("p90", snapshot.p90());
                map.put("p99", snapshot.p99());

                System.out.println("[ConsoleExporter] " + map);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() {
        // در نسخه پیشرفته می‌توانیم flag برای توقف thread اضافه کنیم
    }
}

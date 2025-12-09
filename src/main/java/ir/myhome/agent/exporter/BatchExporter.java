package ir.myhome.agent.exporter;

import ir.myhome.agent.metrics.MetricCollector;
import ir.myhome.agent.metrics.MetricCollectorSingleton;
import ir.myhome.agent.metrics.MetricSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public final class BatchExporter implements Exporter, Runnable {

    private final BlockingQueue<MetricSnapshot> queue;

    public BatchExporter() {
        MetricCollector collector = MetricCollectorSingleton.get();
        this.queue = collector.getExportQueue();
    }

    /** سازگار با Exporter قدیمی */
    @Override
    public void export(Map<String, Object> span) {
        try {
            // تبدیل Map به MetricSnapshot اختیاری است
            // اگر بخواهی می‌توانی اینجا snapshot واقعی enqueue کنی
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** حلقه اصلی برای export snapshot */
    @Override
    public void run() {
        try {
            while (true) {
                MetricSnapshot snapshot = queue.take();

                // تبدیل snapshot به Map برای سازگاری با Exporter
                Map<String, Object> map = new HashMap<>();
                map.put("metricName", snapshot.metricName());
                map.put("count", snapshot.count());
                map.put("p50", snapshot.p50());
                map.put("p90", snapshot.p90());
                map.put("p99", snapshot.p99());

                // چاپ یا ارسال واقعی
                System.out.println("[BatchExporter] " + map);

                // اگر بخواهی export HTTP یا DB اضافه کنی، همین map را می‌توان استفاده کرد
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() {
        // می‌توانیم flag برای توقف thread اضافه کنیم
    }
}

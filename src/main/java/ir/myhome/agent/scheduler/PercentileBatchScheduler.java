package ir.myhome.agent.scheduler;

import ir.myhome.agent.collector.PercentileCollector;
import ir.myhome.agent.exporter.impl.PercentileExporter;

public final class PercentileBatchScheduler {

    private static PercentileCollector collector;
    private static Thread schedulerThread;
    private static long flushIntervalMs;
    // پرچم برای توقف
    private static volatile boolean isStopped = false;

    public static PercentileBatchScheduler start(PercentileCollector percentileCollector, long intervalMs) {
        collector = percentileCollector;
        flushIntervalMs = intervalMs;

        if (schedulerThread != null && schedulerThread.isAlive()) return null;

        schedulerThread = new Thread(() -> {
            PercentileExporter exporter = new PercentileExporter(collector, flushIntervalMs);
            Thread exporterThread = new Thread(exporter, "PercentileExporterThread");
            exporterThread.setDaemon(true);
            exporterThread.start();

            while (!isStopped) {  // چک کردن پرچم توقف
                try {
                    Thread.sleep(1000);  // مدت زمان اجرا برای هر بار
                } catch (InterruptedException e) {
                    if (isStopped) break;  // اگر پرچم توقف فعال باشد، ترد متوقف می‌شود
                }
            }

            exporter.shutdown();  // خاموش کردن exporter
        }, "PercentileBatchScheduler");

        schedulerThread.setDaemon(true);
        schedulerThread.start();

        return new PercentileBatchScheduler();  // بازگرداندن نمونه برای کنترل بیشتر
    }

    // متد برای توقف منطقی
    public static void stop() {
        isStopped = true;  // فعال کردن پرچم توقف
        if (schedulerThread != null) {
            schedulerThread.interrupt();  // متوقف کردن ترد
            System.out.println("PercentileBatchScheduler stopped.");
        }
    }

    public static void record(long durationMs) {
        if (collector != null) {
            collector.record(durationMs);
        }
    }
}

package ir.myhome.agent.exporter;

import ir.myhome.agent.collector.PercentileCollector;

import java.util.Locale;

/**
 * Runnable که هر flushIntervalMs یک‌بار نمونه‌ها را می‌گیرد،
 * بر اساس endpoint (یا service#endpoint) گروه‌بندی می‌کند
 * و P50/P90/P99 را محاسبه و لاگ می‌کند.
 */
public final class PercentileExporter implements Runnable {

    private final PercentileCollector collector;
    private final long flushIntervalMs;
    private volatile boolean running = true;

    public PercentileExporter(PercentileCollector collector, long flushIntervalMs) {
        this.collector = collector;
        this.flushIntervalMs = flushIntervalMs;
    }

    @Override
    public void run() {
        try {
            while (running) {
                try {
                    exportPercentiles();
                    Thread.sleep(flushIntervalMs);
                } catch (InterruptedException ignored) {
                    break;
                } catch (Throwable t) {
                    System.err.println("[PercentileExporter] error: " + t.getMessage());
                    t.printStackTrace();
                }
            }
        } finally {
            System.out.println("[PercentileExporter] stopped");
        }
    }

    private void exportPercentiles() {
        double p50 = collector.percentile(50);
        double p90 = collector.percentile(90);
        double p99 = collector.percentile(99);
        long count = collector.count();

        System.out.printf(Locale.ROOT, "[PercentileExport] samples=%d P50=%.2fms P90=%.2fms P99=%.2fms%n", count, p50, p90, p99);
    }

    public void shutdown() {
        running = false;
    }
}

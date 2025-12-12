package ir.myhome.agent.exporter;

import ir.myhome.agent.collector.PercentileCollector;
import ir.myhome.agent.metrics.PercentileSample;
import ir.myhome.agent.util.PercentileCalculator;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Runnable که هر flushIntervalMs یک‌بار نمونه‌ها را می‌گیرد،
 * بر اساس endpoint (یا service#endpoint) گروه‌بندی می‌کند
 * و P50/P90/P99 را محاسبه و لاگ می‌کند.
 */
public final class PercentileExporter implements Runnable {

    private final long flushIntervalMs;
    private volatile boolean running = true;

    public PercentileExporter(long flushIntervalMs) {
        this.flushIntervalMs = flushIntervalMs;
    }

    @Override
    public void run() {
        try {
            while (running) {
                List<PercentileSample> batch = PercentileCollector.drainSnapshot();
                if (!batch.isEmpty()) {
                    try {
                        processBatch(batch);
                    } catch (Throwable t) {
                        System.err.println("[PercentileExporter] processing error: " + t.getMessage());
                        t.printStackTrace();
                    }
                }
                TimeUnit.MILLISECONDS.sleep(flushIntervalMs);
            }
        } catch (InterruptedException ignored) {
            // thread interrupted -> exit
        } finally {
            System.out.println("[PercentileExporter] stopped");
        }
    }

    private void processBatch(List<PercentileSample> batch) {
        // group by key (service#endpoint)
        Map<String, List<Long>> buckets = new HashMap<>();
        for (PercentileSample s : batch) {
            String key = s.service + "#" + s.endpoint;
            buckets.computeIfAbsent(key, k -> new ArrayList<>()).add(s.durationMs);
        }

        // for each bucket compute percentiles and log
        for (Map.Entry<String, List<Long>> e : buckets.entrySet()) {
            String key = e.getKey();
            List<Long> durations = e.getValue();
            long[] arr = durations.stream().mapToLong(Long::longValue).toArray();

            double p50 = PercentileCalculator.percentile(arr, 50.0);
            double p90 = PercentileCalculator.percentile(arr, 90.0);
            double p99 = PercentileCalculator.percentile(arr, 99.0);

            // split key back
            int sep = key.indexOf('#');
            String service = sep >= 0 ? key.substring(0, sep) : "unknown";
            String endpoint = sep >= 0 ? key.substring(sep + 1) : key;

            System.out.printf(Locale.ROOT, "[PercentileExport] service=%s endpoint=%s samples=%d p50=%.2fms p90=%.2fms p99=%.2fms%n", service, endpoint, arr.length, p50, p90, p99);
        }
    }

    public void shutdown() {
        running = false;
    }
}

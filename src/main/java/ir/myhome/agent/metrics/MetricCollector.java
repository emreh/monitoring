package ir.myhome.agent.metrics;

import ir.myhome.agent.config.AgentConfig;
import org.HdrHistogram.Histogram;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

public final class MetricCollector {

    private final AgentConfig cfg;
    private final ConcurrentMap<String, MetricData> metrics = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Histogram> histogramMap = new ConcurrentHashMap<>();
    private final BlockingQueue<MetricSnapshot> exportQueue;

    public MetricCollector(AgentConfig cfg) {
        this.cfg = cfg;
        this.exportQueue = new LinkedBlockingQueue<>(cfg.queueCapacity);
    }

    public void recordLatency(String metricName, long durationMs) {
        if (!cfg.enableAdvancedMetrics) return;

        long value = Math.min(durationMs, cfg.percentileMaxValueMs);
        histogramMap.computeIfAbsent(metricName, k -> new Histogram(cfg.percentileMaxValueMs, cfg.percentilePrecision)).recordValue(value);
    }

    public void incrementCount(String metricName) {
        metrics.computeIfAbsent(metricName, k -> new MetricData()).incrementCount();
    }

    public void incrementError(String metricName) {
        metrics.computeIfAbsent(metricName, k -> new MetricData()).incrementError();
    }

    public void enqueueForExport(String metricName) {
        try {
            MetricSnapshot snapshot = new MetricSnapshot(metricName, getCount(metricName), getErrorCount(metricName),
                    getPercentile(metricName, 50), getPercentile(metricName, 90),
                    getPercentile(metricName, 99));
            exportQueue.put(snapshot);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public BlockingQueue<MetricSnapshot> getExportQueue() {
        return exportQueue;
    }

    public long getPercentile(String metricName, double percentile) {
        Histogram histogram = histogramMap.get(metricName);
        return histogram != null ? histogram.getValueAtPercentile(percentile) : 0;
    }

    public long getCount(String metricName) {
        MetricData data = metrics.get(metricName);
        return data != null ? data.getCount() : 0;
    }

    public long getErrorCount(String metricName) {
        MetricData data = metrics.get(metricName);
        return data != null ? data.getErrorCount() : 0;
    }
}

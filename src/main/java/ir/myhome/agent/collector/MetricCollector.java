package ir.myhome.agent.collector;

import com.tdunning.math.stats.TDigest;
import ir.myhome.agent.config.AgentConfig;
import ir.myhome.agent.metrics.MetricData;
import ir.myhome.agent.metrics.MetricSnapshot;
import ir.myhome.agent.status.MonitoringServer;
import org.HdrHistogram.Histogram;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

public final class MetricCollector {

    private final AgentConfig cfg;
    private final ConcurrentMap<String, MetricData> metrics = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Histogram> histogramMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, TDigest> tDigestMap = new ConcurrentHashMap<>();
    private final BlockingQueue<MetricSnapshot> exportQueue;

    public MetricCollector(AgentConfig cfg) {
        this.cfg = cfg;
        this.exportQueue = new LinkedBlockingQueue<>(cfg.queueCapacity);
    }

    public void recordLatency(String metricName, long durationMs) {
        if (!cfg.enableAdvancedMetrics) return;

        MonitoringServer.TOTAL_SPANS.incrementAndGet();

        long value = Math.min(durationMs, cfg.percentileMaxValueMs);
        histogramMap.computeIfAbsent(metricName, k -> new Histogram(cfg.percentileMaxValueMs, cfg.percentilePrecision)).recordValue(value);
        tDigestMap.computeIfAbsent(metricName, k -> TDigest.createDigest(100)).add(value);
    }

    public void incrementCount(String metricName) {
        MonitoringServer.TOTAL_SPANS.incrementAndGet();
        metrics.computeIfAbsent(metricName, k -> new MetricData()).incrementCount();
    }

    public void incrementError(String metricName) {
        MonitoringServer.TOTAL_SPANS.incrementAndGet();
        metrics.computeIfAbsent(metricName, k -> new MetricData()).incrementError();
    }

    public void enqueueForExport(String metricName) {
        try {
            Histogram hist = histogramMap.get(metricName);
            TDigest digest = tDigestMap.get(metricName);

            MetricSnapshot snapshot = new MetricSnapshot(metricName, getCount(metricName), getErrorCount(metricName), hist != null ? hist.getValueAtPercentile(50) : 0, hist != null ? hist.getValueAtPercentile(90) : 0, hist != null ? hist.getValueAtPercentile(99) : 0, digest != null ? digest.quantile(0.50) : 0, digest != null ? digest.quantile(0.90) : 0, digest != null ? digest.quantile(0.99) : 0);

            exportQueue.put(snapshot);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public BlockingQueue<MetricSnapshot> getExportQueue() {
        return exportQueue;
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

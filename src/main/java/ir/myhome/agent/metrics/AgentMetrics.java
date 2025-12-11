package ir.myhome.agent.metrics;

import com.tdunning.math.stats.TDigest;
import org.HdrHistogram.Histogram;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class AgentMetrics {

    private final AtomicInteger queueSize = new AtomicInteger(0);
    final ConcurrentMap<String, AtomicLong> counters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Histogram> hdrHistograms = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, TDigest> tDigests = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<MetricEvent> exportQueue = new ConcurrentLinkedQueue<>();

    public int incrementQueue() {
        return queueSize.incrementAndGet();
    }

    public int decrementQueue() {
        return queueSize.decrementAndGet();
    }

    public int getQueueSize() {
        return queueSize.get();
    }

    public void incrementCounter(String metricName) {
        counters.computeIfAbsent(metricName, k -> new AtomicLong(0)).incrementAndGet();
        exportQueue.add(new MetricEvent(metricName, MetricType.COUNTER, 1));
    }

    public long getCounter(String metricName) {
        return counters.getOrDefault(metricName, new AtomicLong(0)).get();
    }

    public void recordLatency(String metricName, long latencyMs) {
        hdrHistograms.computeIfAbsent(metricName, k -> new Histogram(3600000, 3)).recordValue(latencyMs);
        tDigests.computeIfAbsent(metricName, k -> TDigest.createDigest(100)).add(latencyMs);
        exportQueue.add(new MetricEvent(metricName, MetricType.LATENCY, latencyMs));
    }

    public long getHdrPercentile(String metricName, double percentile) {
        Histogram hist = hdrHistograms.get(metricName);
        return hist != null ? hist.getValueAtPercentile(percentile) : 0;
    }

    public double getTDigestPercentile(String metricName, double percentile) {
        TDigest digest = tDigests.get(metricName);
        return digest != null ? digest.quantile(percentile / 100.0) : 0;
    }

    public ConcurrentLinkedQueue<MetricEvent> drainExportQueue() {
        ConcurrentLinkedQueue<MetricEvent> drained = new ConcurrentLinkedQueue<>();
        MetricEvent event;
        while ((event = exportQueue.poll()) != null) drained.add(event);
        return drained;
    }

    public void resetMetric(String metricName) {
        counters.remove(metricName);
        hdrHistograms.remove(metricName);
        tDigests.remove(metricName);
    }

    public void resetAll() {
        queueSize.set(0);
        counters.clear();
        hdrHistograms.clear();
        tDigests.clear();
        exportQueue.clear();
    }

    public enum MetricType {COUNTER, LATENCY}

    public static final class MetricEvent {
        public final String name;
        public final MetricType type;
        public final long value;

        public MetricEvent(String name, MetricType type, long value) {
            this.name = name;
            this.type = type;
            this.value = value;
        }
    }

    public ConcurrentMap<String, AtomicLong> getCounters() {
        return counters;
    }
}
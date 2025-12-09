package ir.myhome.agent.metrics;

public record MetricSnapshot(String metricName, long count, long errorCount, long p50, long p90, long p99) {
}

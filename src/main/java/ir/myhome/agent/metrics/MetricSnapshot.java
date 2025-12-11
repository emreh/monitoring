package ir.myhome.agent.metrics;

public final class MetricSnapshot {

    private final String metricName;
    private final long count;
    private final long errorCount;

    // HDRHistogram Percentiles
    private final long p50;
    private final long p90;
    private final long p99;

    // TDigest Percentiles
    private final double tdigestP50;
    private final double tdigestP90;
    private final double tdigestP99;

    public MetricSnapshot(
            String metricName, long count, long errorCount,
            long p50, long p90, long p99,
            double tdigestP50, double tdigestP90, double tdigestP99) {
        this.metricName = metricName;
        this.count = count;
        this.errorCount = errorCount;
        this.p50 = p50;
        this.p90 = p90;
        this.p99 = p99;
        this.tdigestP50 = tdigestP50;
        this.tdigestP90 = tdigestP90;
        this.tdigestP99 = tdigestP99;
    }

    // Getters
    public String getMetricName() { return metricName; }
    public long getCount() { return count; }
    public long getErrorCount() { return errorCount; }
    public long getP50() { return p50; }
    public long getP90() { return p90; }
    public long getP99() { return p99; }
    public double getTDigestP50() { return tdigestP50; }
    public double getTDigestP90() { return tdigestP90; }
    public double getTDigestP99() { return tdigestP99; }

    @Override
    public String toString() {
        return "MetricSnapshot[" +
                "metricName=" + metricName +
                ", count=" + count +
                ", errorCount=" + errorCount +
                ", p50=" + p50 +
                ", p90=" + p90 +
                ", p99=" + p99 +
                ", tdigestP50=" + tdigestP50 +
                ", tdigestP90=" + tdigestP90 +
                ", tdigestP99=" + tdigestP99 +
                "]";
    }
}

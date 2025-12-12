package ir.myhome.agent.collector;

public final class PercentileOrchestrator implements PercentileCollector {

    private final HDRHistogramCollector hdrCollector;
    private final TDigestCollector tDigestCollector;

    public PercentileOrchestrator(HDRHistogramCollector hdrCollector, TDigestCollector tDigestCollector) {
        this.hdrCollector = hdrCollector;
        this.tDigestCollector = tDigestCollector;
    }

    @Override
    public void record(long value) {
        hdrCollector.record(value);
        tDigestCollector.record(value);
    }

    @Override
    public double percentile(double p) {
        // می‌توان یکی را اولویت داد یا میانگین گرفت
        double h = hdrCollector.percentile(p);
        double t = tDigestCollector.percentile(p);
        return (h + t) / 2; // نمونه: میانگین
    }

    @Override
    public long count() {
        return Math.max(hdrCollector.count(), tDigestCollector.count());
    }

    public HDRHistogramCollector getHdr() {
        return hdrCollector;
    }

    public TDigestCollector getTDigest() {
        return tDigestCollector;
    }
}

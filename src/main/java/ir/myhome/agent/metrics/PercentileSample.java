package ir.myhome.agent.metrics;

public final class PercentileSample {
    public final String service;
    public final String endpoint;
    public final long durationMs;
    public final long ts; // timestamp when recorded

    public PercentileSample(String service, String endpoint, long durationMs, long ts) {
        this.service = service == null ? "unknown" : service;
        this.endpoint = endpoint == null ? "unknown" : endpoint;
        this.durationMs = durationMs;
        this.ts = ts;
    }

    @Override
    public String toString() {
        return "PercentileSample{" +
                "service='" + service + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", durationMs=" + durationMs +
                ", ts=" + ts +
                '}';
    }
}

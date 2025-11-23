package ir.myhome.agent.core;

public final class Span {

    public final String traceId;
    public final String spanId;
    public final String parentId;
    public final String service;
    public final String endpoint;

    public final long startEpochMs;
    public long durationMs;
    public String status;

    public Span(String traceId, String spanId, String parentId, String service, String endpoint, long startEpochMs) {
        this.traceId = traceId;
        this.spanId = spanId;
        this.parentId = parentId;
        this.service = service;
        this.endpoint = endpoint;
        this.startEpochMs = startEpochMs;
        this.status = "OK";
    }

    public void end() {
        this.durationMs = Math.max(0, System.currentTimeMillis() - this.startEpochMs);

        if (this.status == null)
            this.status = "OK";
    }
}

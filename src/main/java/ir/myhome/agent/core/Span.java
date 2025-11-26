package ir.myhome.agent.core;

public final class Span {

    private final String traceId;
    private final String spanId;
    private final String parentId;
    private final String service;
    private final String endpoint;

    public final long startEpochMs;
    public long durationMs;
    public String status = "OK";
    public String errorMessage = null;

    public Span(String traceId, String spanId, String parentId, String service, String endpoint, long startEpochMs) {
        this.traceId = traceId;
        this.spanId = spanId;
        this.parentId = parentId;
        this.service = service;
        this.endpoint = endpoint;
        this.startEpochMs = startEpochMs;
    }

    public void end() {
        this.durationMs = Math.max(0, System.currentTimeMillis() - this.startEpochMs);
        if (status == null) status = "OK";
    }

    public void markError(String message) {
        this.status = "ERROR";
        this.errorMessage = message;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public String getParentId() {
        return parentId;
    }

    public String getService() {
        return service;
    }

    public String getEndpoint() {
        return endpoint;
    }
}

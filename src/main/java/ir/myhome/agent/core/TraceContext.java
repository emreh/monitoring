package ir.myhome.agent.core;

import java.util.UUID;

public final class TraceContext {

    private final String traceId;
    private final String spanId;
    private final String parentSpanId;

    public TraceContext(String traceId, String spanId, String parentSpanId) {
        this.traceId = traceId;
        this.spanId = spanId;
        this.parentSpanId = parentSpanId;
    }

    public static TraceContext root() {
        return new TraceContext(UUID.randomUUID().toString(), UUID.randomUUID().toString(), null);
    }

    public TraceContext child() {
        return new TraceContext(traceId, UUID.randomUUID().toString(), spanId);
    }

    public String getTraceId() {
        return traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public String getParentSpanId() {
        return parentSpanId;
    }
}

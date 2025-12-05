package ir.myhome.agent.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Span {

    public final String traceId;
    public final String spanId;
    public final String parentId;
    public String service;
    public String endpoint;
    public String statusCode;
    public final long startEpochMs;
    public long durationMs;
    public String status = "OK";
    public String errorMessage;
    public final Map<String, String> tags = new ConcurrentHashMap<>();

    public Span(String traceId, String spanId, String parentId, String service, String endpoint, long startEpochMs) {
        this.traceId = traceId;
        this.spanId = spanId;
        this.parentId = parentId;
        this.service = service == null ? "unknown" : service;
        this.endpoint = endpoint == null ? "unknown" : endpoint;
        this.startEpochMs = startEpochMs;
    }

    public void end() {
        this.durationMs = Math.max(0, System.currentTimeMillis() - this.startEpochMs);
        if (status == null) status = "OK";
    }

    public void markError(String msg) {
        this.status = "ERROR";
        this.errorMessage = msg;
    }

    public void addTag(String k, String v) {
        if (k != null && v != null) tags.put(k, v);
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = String.valueOf(statusCode);
    }

    @Override
    public String toString() {
        return "Span{" +
                "traceId='" + traceId + '\'' +
                ", spanId='" + spanId + '\'' +
                ", parentId='" + parentId + '\'' +
                ", service='" + service + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", statusCode='" + statusCode + '\'' +
                ", startEpochMs=" + startEpochMs +
                ", durationMs=" + durationMs +
                ", status='" + status + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", tags=" + tags +
                '}';
    }
}

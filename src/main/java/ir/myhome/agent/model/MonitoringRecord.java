package ir.myhome.agent.model;

import java.util.HashMap;
import java.util.Map;

public class MonitoringRecord {
    private String id;
    private long timestamp;
    private String source;
    private String type;
    private Map<String, String> tags = new HashMap<>();
    private Map<String, Number> metrics = new HashMap<>();
    private Map<String, Object> context = new HashMap<>();

    public MonitoringRecord() {
    }

    public MonitoringRecord(String id, long timestamp, String source, String type) {
        this.id = id;
        this.timestamp = timestamp;
        this.source = source;
        this.type = type;
    }

    // getters / setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public Map<String, Number> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, Number> metrics) {
        this.metrics = metrics;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }
}

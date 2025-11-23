package ir.myhome.agent;

import ir.myhome.agent.model.MonitoringRecord;
import ir.myhome.agent.queue.RecordProducer;

import java.util.Map;
import java.util.UUID;

public class MonitoringAgent {

    private final RecordProducer<MonitoringRecord> producer;
    private final String source;

    public MonitoringAgent(RecordProducer<MonitoringRecord> producer, String source) {
        this.producer = producer;
        this.source = source;
    }

    public void log(String type, Map<String, Object> context) {
        MonitoringRecord r = new MonitoringRecord(UUID.randomUUID().toString(), System.currentTimeMillis(), source, type);

        if (context != null) {
            context.forEach((k, v) -> r.getContext().put(k, v));
        }

        producer.submit(r);
    }
}

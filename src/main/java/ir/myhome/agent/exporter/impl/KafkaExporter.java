package ir.myhome.agent.exporter.impl;

import ir.myhome.agent.core.Span;
import ir.myhome.agent.exporter.AgentExporter;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.List;
import java.util.Properties;

public final class KafkaExporter implements AgentExporter {

    private final KafkaProducer<String, String> producer;
    private final String topic;

    public KafkaExporter(String bootstrapServers, String topic) {
        this.topic = (topic != null) ? topic : "agent-spans";
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        // تنظیمات برای پایداری ایجنت (مطیع بودن)
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 5000);

        this.producer = new KafkaProducer<>(props);
    }

    @Override
    public void export(List<Span> batch) {
        for (Span span : batch) {
            // استفاده دقیق از فیلدهای کلاس Span تو
            String message = String.format("{\"traceId\":\"%s\",\"spanId\":\"%s\",\"service\":\"%s\",\"endpoint\":\"%s\",\"durationMs\":%d,\"status\":\"%s\"}", span.traceId, span.spanId, span.service, span.endpoint, span.durationMs, span.status);

            producer.send(new ProducerRecord<>(topic, span.traceId, message));
        }
    }
}
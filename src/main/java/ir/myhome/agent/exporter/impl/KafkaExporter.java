package ir.myhome.agent.exporter.impl;

import ir.myhome.agent.core.Aggregator;
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
    private boolean verbose;  // اضافه کردن ویژگی verbose برای نمایش اطلاعات بیشتر
    private Aggregator aggregator;

    public KafkaExporter(String bootstrapServers, String topic, boolean verbose, Aggregator aggregator) {
        this.topic = (topic != null) ? topic : "agent-spans";
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        this.producer = new KafkaProducer<>(props);
        this.verbose = verbose;
        this.aggregator = aggregator;
    }

    @Override
    public void export(List<Span> batch) {
        if (batch == null || batch.isEmpty()) {
            System.out.println("[KafkaExporter] No spans to export.");
            return;
        }

        for (Span span : batch) {
            String message = String.format("{\"traceId\":\"%s\",\"spanId\":\"%s\",\"service\":\"%s\",\"endpoint\":\"%s\",\"durationMs\":%d,\"status\":\"%s\"}",
                    span.traceId, span.spanId, span.service, span.endpoint, span.durationMs, span.status);

            if (verbose) {
                message += String.format(", \"tags\":%s, \"errorMessage\":%s", span.tags, span.errorMessage);
            }

            // محاسبه درصدها با استفاده از Aggregator
            double p50 = aggregator.getPercentile(50);  // محاسبه P50
            double p95 = aggregator.getPercentile(95);  // محاسبه P95
            double p99 = aggregator.getPercentile(99);  // محاسبه P99

            message += String.format(" P50: %.2f, P95: %.2f, P99: %.2f", p50, p95, p99);

            producer.send(new ProducerRecord<>(topic, String.valueOf(span.traceId), message));

            // چاپ اطلاعات verbose در صورت نیاز
            if (verbose) {
                System.out.println("[KafkaExporter] Sent message: " + message);
            }
        }
    }

    // متد برای تنظیم حالت verbose
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    // متد برای گرفتن وضعیت verbose
    public boolean isVerbose() {
        return verbose;
    }
}

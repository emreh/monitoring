package ir.myhome.agent.exporter.impl;

import ir.myhome.agent.core.Aggregator;
import ir.myhome.agent.core.Span;
import ir.myhome.agent.exporter.AgentExporter;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class HttpExporter implements AgentExporter {

    private final String endpoint;
    private boolean verbose;
    private Aggregator aggregator;

    public HttpExporter(String endpoint, boolean verbose, Aggregator aggregator) {
        this.endpoint = endpoint;
        this.verbose = verbose;
        this.aggregator = aggregator;
    }

    @Override
    public void export(List<Span> spans) {
        if (spans == null || spans.isEmpty()) {
            System.out.println("[HttpExporter] No spans to export.");
            return;
        }

        for (Span span : spans) {
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

            try {
                URL url = new URL(endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                byte[] payload = message.getBytes(StandardCharsets.UTF_8);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload);
                }

                int code = conn.getResponseCode();
                if (code != 200) {
                    System.err.println("[HttpExporter] Send failed: HTTP " + code);
                }

                if (verbose) {
                    System.out.println("[HttpExporter] Sent message: " + message);
                }
            } catch (Exception e) {
                System.err.println("[HttpExporter] Error sending to endpoint: " + e.getMessage());
            }
        }
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isVerbose() {
        return verbose;
    }
}

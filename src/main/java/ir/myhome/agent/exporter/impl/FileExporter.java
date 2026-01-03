package ir.myhome.agent.exporter.impl;

import ir.myhome.agent.core.Aggregator;
import ir.myhome.agent.core.Span;
import ir.myhome.agent.exporter.AgentExporter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class FileExporter implements AgentExporter {

    private final String filePath = "agent-metrics.log";
    private boolean verbose;
    private Aggregator aggregator;

    public FileExporter(boolean verbose, Aggregator aggregator) {
        this.verbose = verbose;
        this.aggregator = aggregator;
    }

    @Override
    public void export(List<Span> batch) {
        if (batch == null || batch.isEmpty()) {
            System.out.println("[FileExporter] No spans to export.");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            for (Span span : batch) {
                String output = String.format("Span: traceId=%s, spanId=%s, service=%s, endpoint=%s, duration=%d ms, status=%s", span.traceId, span.spanId, span.service, span.endpoint, span.durationMs, span.status);

                if (verbose) {
                    output += String.format(", tags=%s, errorMessage=%s", span.tags, span.errorMessage);
                }

                // محاسبه درصدها با استفاده از Aggregator
                double p50 = aggregator.getPercentile(50);  // محاسبه P50
                double p95 = aggregator.getPercentile(95);  // محاسبه P95
                double p99 = aggregator.getPercentile(99);  // محاسبه P99

                output += String.format(" P50: %.2f, P95: %.2f, P99: %.2f", p50, p95, p99);


                writer.write(output);
                writer.newLine();

                // چاپ اطلاعات verbose در صورت نیاز
                if (verbose) {
                    System.out.println("[FileExporter] Writing span to file: " + output);
                }
            }
        } catch (IOException e) {
            System.err.println("[FileExporter] Error writing to file: " + e.getMessage());
        }
    }

    // متد برای تنظیم حالت verbose
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    // متد برای دریافت وضعیت verbose
    public boolean isVerbose() {
        return verbose;
    }
}

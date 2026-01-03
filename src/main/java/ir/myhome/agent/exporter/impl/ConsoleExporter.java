package ir.myhome.agent.exporter.impl;

import ir.myhome.agent.core.Aggregator;
import ir.myhome.agent.core.Span;
import ir.myhome.agent.exporter.AgentExporter;

import java.util.List;

public class ConsoleExporter implements AgentExporter {

    // قابلیت تنظیم برای چاپ اطلاعات به‌صورت جزئی
    private boolean verbose;
    private Aggregator aggregator;

    // سازنده برای تنظیم حالت verbose
    public ConsoleExporter(boolean verbose, Aggregator aggregator) {
        this.aggregator = aggregator;
        this.verbose = verbose;
    }

    @Override
    public void export(List<Span> batch) {
        if (batch == null || batch.isEmpty()) {
            System.out.println("[ConsoleExporter] No spans to export.");
            return;
        }

        for (Span span : batch) {
            // چاپ اطلاعات پایه اسپان
            String output = String.format("[ConsoleExporter] Span: traceId=%s, spanId=%s, service=%s, endpoint=%s, duration=%d ms, status=%s", span.traceId, span.spanId, span.service, span.endpoint, span.durationMs, span.status);

            // اگر حالت verbose فعال باشد، اطلاعات بیشتری چاپ می‌کنیم
            if (verbose) {
                output += String.format(", tags=%s, errorMessage=%s", span.tags, span.errorMessage);
            }

            // نمایش درصدهای مختلف (P50, P95, P99)
            double p50 = aggregator.getPercentile(50);  // محاسبه P50
            double p95 = aggregator.getPercentile(95);  // محاسبه P95
            double p99 = aggregator.getPercentile(99);  // محاسبه P99

            output += String.format(" P50: %.2f, P95: %.2f, P99: %.2f", p50, p95, p99);

            System.out.println(output);  // چاپ خروجی به کنسول
        }
    }

    // متد برای تغییر حالت verbose
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    // متد برای فعال یا غیرفعال کردن verbose
    public boolean isVerbose() {
        return verbose;
    }
}

package ir.myhome.agent.reporter;

import ir.myhome.agent.collector.SpanCollector;
import ir.myhome.agent.core.Span;

import java.util.List;

public class SpanReporter {

    private final SpanCollector collector;

    public SpanReporter(SpanCollector collector) {
        this.collector = collector;
    }

    public void reportSummary() {
        long totalSpans = collector.getCount();
        double p50 = collector.getPercentile(0.5);
        double p95 = collector.getPercentile(0.95);
        double p99 = collector.getPercentile(0.99);

        System.out.println("===== Span Report =====");
        System.out.println("Total Spans: " + totalSpans);
        System.out.println("P50 Duration: " + p50 + " ms");
        System.out.println("P95 Duration: " + p95 + " ms");
        System.out.println("P99 Duration: " + p99 + " ms");
        System.out.println("=======================");
    }

    public void reportSpans(List<Span> spans) {
        spans.forEach(System.out::println);
    }
}

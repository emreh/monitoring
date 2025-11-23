package ir.myhome.agent;

import ir.myhome.agent.core.Span;
import ir.myhome.agent.core.TraceState;
import ir.myhome.agent.instrumentation.advice.ExecutorTraceAdvice;

public class TraceAwareRunnable implements Runnable {

    private final Runnable delegate;
    private final String traceId;
    private final String parentSpanId;

    public TraceAwareRunnable(Runnable delegate, String traceId, String parentSpanId) {
        this.delegate = delegate;
        this.traceId = traceId;
        this.parentSpanId = parentSpanId;
    }

    @Override
    public void run() {
        if (traceId != null) TraceState.setTraceId(traceId);
        if (parentSpanId != null) TraceState.pushSpan(parentSpanId);

        String spanId = java.util.UUID.randomUUID().toString();
        TraceState.pushSpan(spanId);
        long start = System.currentTimeMillis();

        try {
            delegate.run();
        } finally {
            long duration = System.currentTimeMillis() - start;
            Span span = new Span(TraceState.getTraceId(), spanId, parentSpanId, "unknown-service", "executor-task", start);
            span.durationMs = duration;
            span.status = "SUCCESS";

            if (ExecutorTraceAdvice.exporter != null) {
                ExecutorTraceAdvice.exporter.export(span);
            }

            TraceState.popSpan();
            if (parentSpanId != null) TraceState.popSpan();
        }
    }
}

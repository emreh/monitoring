package ir.myhome.agent;

import ir.myhome.agent.core.Span;
import ir.myhome.agent.core.TraceState;
import ir.myhome.agent.instrumentation.advice.ExecutorTraceAdvice;

import java.util.concurrent.Callable;

public class TraceAwareCallable<V> implements Callable<V> {

    private final Callable<V> delegate;
    private final String traceId;
    private final String parentSpanId;

    public TraceAwareCallable(Callable<V> delegate, String traceId, String parentSpanId) {
        this.delegate = delegate;
        this.traceId = traceId;
        this.parentSpanId = parentSpanId;
    }

    @Override
    public V call() throws Exception {
        if (traceId != null)
            TraceState.setTraceId(traceId);

        if (parentSpanId != null)
            TraceState.pushSpan(parentSpanId);

        String spanId = java.util.UUID.randomUUID().toString();
        TraceState.pushSpan(spanId);
        long start = System.currentTimeMillis();

        try {
            V out = delegate.call();
            long duration = System.currentTimeMillis() - start;
            Span span = new Span(TraceState.getTraceId(), spanId, parentSpanId, "unknown-service", "executor-task", start);
            span.durationMs = duration;
            span.status = "SUCCESS";

            if (ExecutorTraceAdvice.exporter != null) {
                ExecutorTraceAdvice.exporter.export(span);
            }
            return out;
        } finally {
            TraceState.popSpan();

            if (parentSpanId != null)
                TraceState.popSpan();
        }
    }
}

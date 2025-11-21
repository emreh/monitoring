package ir.myhome.agent;

import ir.myhome.agent.core.Span;
import ir.myhome.agent.core.SpanExporter;
import ir.myhome.agent.core.TraceState;
import ir.myhome.agent.instrumentation.ExecutorTraceAdvice;

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

        if (traceId != null) TraceState.setTraceId(traceId);
        if (parentSpanId != null) TraceState.pushSpan(parentSpanId);

        String spanId = java.util.UUID.randomUUID().toString();
        TraceState.pushSpan(spanId);
        long start = System.currentTimeMillis();

        try {
            return delegate.call();
        } finally {

            long duration = System.currentTimeMillis() - start;

            Span span = new Span(
                    TraceState.getTraceId(),
                    spanId,
                    parentSpanId,
                    "unknown-service",
                    "executor-task",
                    start
            );
            span.durationMs = duration;

            SpanExporter ex = ExecutorTraceAdvice.exporter;
            if (ex != null) ex.export(span);

            TraceState.popSpan();
            if (parentSpanId != null) TraceState.popSpan();
        }
    }
}

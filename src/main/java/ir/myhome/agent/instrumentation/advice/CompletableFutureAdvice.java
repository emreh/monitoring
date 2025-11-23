package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.core.Span;
import ir.myhome.agent.core.TraceState;
import net.bytebuddy.asm.Advice;

import java.util.function.Supplier;

public class CompletableFutureAdvice {

    @Advice.OnMethodEnter
    public static void onEnter(@Advice.Argument(value = 0, readOnly = false) Object task) {
        String traceId = TraceState.getTraceId();
        String parent = TraceState.peekSpan();

        if (task instanceof Supplier<?> s) {
            task = (Supplier<?>) () -> {
                if (traceId != null) TraceState.setTraceId(traceId);
                if (parent != null) TraceState.pushSpan(parent);

                String spanId = java.util.UUID.randomUUID().toString();
                TraceState.pushSpan(spanId);
                long start = System.currentTimeMillis();
                try {
                    Object out = s.get();
                    long duration = System.currentTimeMillis() - start;
                    Span sp = new Span(TraceState.getTraceId(), spanId, parent, "unknown-service", "cf-supplier", start);
                    sp.durationMs = duration;
                    if (ExecutorTraceAdvice.exporter != null) ExecutorTraceAdvice.exporter.export(sp);
                    return out;
                } finally {
                    TraceState.popSpan();
                    if (parent != null) TraceState.popSpan();
                }
            };
        } else if (task instanceof Runnable r) {
            task = (Runnable) () -> {
                if (traceId != null) TraceState.setTraceId(traceId);
                if (parent != null) TraceState.pushSpan(parent);

                String spanId = java.util.UUID.randomUUID().toString();
                TraceState.pushSpan(spanId);
                long start = System.currentTimeMillis();
                try {
                    r.run();
                } finally {
                    long duration = System.currentTimeMillis() - start;
                    Span sp = new Span(TraceState.getTraceId(), spanId, parent, "unknown-service", "cf-run", start);
                    sp.durationMs = duration;
                    if (ExecutorTraceAdvice.exporter != null) ExecutorTraceAdvice.exporter.export(sp);
                    TraceState.popSpan();
                    if (parent != null) TraceState.popSpan();
                }
            };
        }
        // ByteBuddy will replace argument with wrapper
    }
}

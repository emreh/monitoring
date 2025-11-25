package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.bootstrap.ExporterHolder;
import ir.myhome.agent.core.Span;
import ir.myhome.agent.core.TraceContextHolder;
import ir.myhome.agent.exporter.SpanExporter;
import net.bytebuddy.asm.Advice;

import java.util.UUID;

public final class TimingAdvice {

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void enter(@Advice.Origin("#t.#m") String signature, @Advice.Local("spanRef") Span[] spanRef) {
        String traceId = TraceContextHolder.currentTraceId();

        if (traceId == null) traceId = UUID.randomUUID().toString();

        String spanId = UUID.randomUUID().toString();
        String parentId = TraceContextHolder.currentSpanId();

        TraceContextHolder.pushSpan(spanId, traceId);

        Span span = new Span(traceId, spanId, parentId, extractService(signature), extractEndpoint(signature), System.currentTimeMillis());
        spanRef[0] = span;
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void exit(@Advice.Thrown Throwable thrown, @Advice.Local("spanRef") Span[] spanRef) {

        if (spanRef == null || spanRef.length == 0) return;

        Span span = spanRef[0];

        if (span == null) return;

        if (thrown != null) span.markError(thrown.getMessage());
        span.end();

        TraceContextHolder.popSpan();

        SpanExporter exporter = ExporterHolder.getExporter();
        if (exporter != null) {
            exporter.export(span);
        }
    }

    private static String extractService(String sig) {
        if (sig == null) return "unknown";
        int idx = sig.indexOf('.');
        return idx > 0 ? sig.substring(0, idx) : sig;
    }

    private static String extractEndpoint(String sig) {
        return sig == null ? "unknown" : sig;
    }
}

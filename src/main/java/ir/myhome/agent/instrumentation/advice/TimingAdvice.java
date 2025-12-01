package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.bootstrap.AgentHolder;
import ir.myhome.agent.core.*;
import net.bytebuddy.asm.Advice;

public final class TimingAdvice {

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void enter(@Advice.Origin("#t.#m") String signature, @Advice.Local("spanRef") Span[] spanRef) {
        try {
            String traceId = TraceContextHolder.currentTraceId();
            String spanId = TraceContext.newId();
            String parent = TraceContextHolder.currentSpanId();
            TraceContextHolder.pushSpan(spanId, traceId);
            Span s = new Span(traceId, spanId, parent, extractService(signature), signature, System.currentTimeMillis());
            spanRef[0] = s;

            if (AgentConstants.DEBUG) System.out.println("[TimingAdvice] enter " + signature + " spanId=" + spanId);
        } catch (Throwable t) {
            System.err.println("[TimingAdvice] enter failed: " + t.getMessage());
        }
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void exit(@Advice.Thrown Throwable thrown, @Advice.Local("spanRef") Span[] spanRef) {
        if (spanRef == null || spanRef.length == 0) return;

        Span span = spanRef[0];

        if (span == null) return;

        try {
            if (thrown != null) span.markError(thrown.getMessage());
        } catch (Throwable ignore) {
        }

        try {
            span.end();
        } catch (Throwable ignore) {
        }

        try {
            TraceContextHolder.popSpan();
        } catch (Throwable ignore) {
        }

        // publish to queue
        try {
            var q = AgentHolder.getSpanQueue();
            if (q != null) q.offer(span);
            else {
                // fallback: print
                System.out.println("[TimingAdvice] fallback span: " + JsonSerializer.toJson(span));
            }
        } catch (Throwable t) {
            System.err.println("[TimingAdvice] publish failed: " + t.getMessage());
        }
    }

    private static String extractService(String sig) {
        if (sig == null) return "unknown";
        int idx = sig.indexOf('.');
        return idx > 0 ? sig.substring(0, idx) : sig;
    }
}

package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.core.Span;
import ir.myhome.agent.core.TraceContext;
import ir.myhome.agent.core.TraceContextHolder;
import ir.myhome.agent.holder.AgentHolder;
import net.bytebuddy.asm.Advice;

public final class TimingAdviceUniversal {

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static Span onEnter(@Advice.Origin("#t.#m") String signature) {
        try {
            String traceId = TraceContextHolder.currentTraceId();
            String spanId = TraceContext.newId();
            String parent = TraceContextHolder.currentSpanId();

            TraceContextHolder.pushSpan(spanId, traceId);

            return new Span(traceId, spanId, parent, extractService(signature), signature, System.currentTimeMillis());
        } catch (Throwable t) {
            System.err.println("[TimingAdvice] enter failed: " + t.getMessage());
            return null;
        }
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void onExit(@Advice.Enter Span span, @Advice.Thrown Throwable thrown) {
        if (span == null) return;

        try {
            if (thrown != null) span.markError(thrown.getMessage());
            span.end();
        } catch (Throwable ignore) {
        }

        try {
            TraceContextHolder.popSpan();
        } catch (Throwable ignore) {
        }

        try {
            var q = AgentHolder.getSpanQueue();

            if (q != null) q.offer(span);
            else System.out.println("[TimingAdvice] fallback: " + span);
        } catch (Throwable ignore) {
        }
    }

    private static String extractService(String sig) {
        if (sig == null) return "unknown";

        int idx = sig.indexOf('.');
        return idx > 0 ? sig.substring(0, idx) : sig;
    }
}

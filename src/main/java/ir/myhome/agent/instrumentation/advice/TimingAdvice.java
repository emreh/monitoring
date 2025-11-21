package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.core.Span;
import ir.myhome.agent.core.SpanExporter;
import ir.myhome.agent.core.TraceState;
import net.bytebuddy.asm.Advice;

import java.util.UUID;

public class TimingAdvice {
    public static volatile SpanExporter exporter;

    @Advice.OnMethodEnter
    public static long onEnter(@Advice.Origin("#t.#m") String origin) {
        String traceId = TraceState.ensureTraceId();
        String parentId = TraceState.peekSpan();
        String spanId = UUID.randomUUID().toString();
        TraceState.pushSpan(spanId);
        System.out.println("[TimingAdvice] ENTER -> " + origin + " thread=" + Thread.currentThread().getName() + " traceId=" + traceId + " parent=" + parentId + " spanId=" + spanId);
        return System.currentTimeMillis();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit(@Advice.Origin("#t.#m") String origin, @Advice.Enter long startMs, @Advice.Thrown Throwable thrown) {
        try {
            long duration = Math.max(0, System.currentTimeMillis() - startMs);
            String spanId = TraceState.peekSpan();
            String parentId = null;
            if (spanId != null) {
                TraceState.popSpan();
                parentId = TraceState.peekSpan();
                if (spanId != null) TraceState.pushSpan(spanId);
            }

            Span s = new Span(TraceState.getTraceId(), spanId, parentId, "unknown-service", origin, startMs);
            s.durationMs = duration;
            s.status = thrown == null ? "SUCCESS" : "ERROR";

            if (exporter != null) exporter.export(s);

            TraceState.popSpan();

        } catch (Throwable t) {
            // swallow
        }
    }
}

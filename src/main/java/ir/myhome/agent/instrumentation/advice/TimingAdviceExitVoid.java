// file: ir/myhome/agent/instrumentation/advice/TimingAdviceExitVoid.java
package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.core.Span;
import ir.myhome.agent.core.TraceContextHolder;
import ir.myhome.agent.holder.AgentHolder;
import ir.myhome.agent.util.JsonSerializer;
import net.bytebuddy.asm.Advice;

public final class TimingAdviceExitVoid {

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void exit(@Advice.Origin("#t.#m") String signature, @Advice.Local("spanRef") Span[] spanRef, @Advice.Thrown Throwable thrown) {
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

        // publish
        try {
            var q = AgentHolder.getSpanQueue();

            if (q != null) q.offer(span);
            else System.out.println("[TimingAdvice] fallback span: " + JsonSerializer.toJson(span));
        } catch (Throwable t) {
            System.err.println("[TimingAdvice] publish failed: " + t.getMessage());
        }
    }
}

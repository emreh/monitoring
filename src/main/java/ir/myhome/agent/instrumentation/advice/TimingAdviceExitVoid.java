package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.config.AgentContext;
import ir.myhome.agent.core.Span;
import ir.myhome.agent.core.TraceContextHolder;
import ir.myhome.agent.holder.AgentHolder;
import net.bytebuddy.asm.Advice;

public final class TimingAdviceExitVoid {

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void exit(@Advice.Local("spanRef") Span[] spanRef, @Advice.Thrown Throwable thrown) {
        handleExit(spanRef, thrown);
    }

    private static void handleExit(Span[] spanRef, Throwable thrown) {
        try {
            System.out.println("[TimingAdviceExitVoid] TimingAdviceExitVoid Started");
            if (spanRef == null || spanRef.length == 0) return;

            Span span = spanRef[0];

            if (span == null) return;

            if (thrown != null) span.markError(thrown.getMessage());

            span.end();
            TraceContextHolder.popSpan();

            var q = AgentHolder.getSpanQueue();
            if (q != null) q.offer(span);
            else System.out.println("[TimingAdviceExitVoid] fallback span: " + span);

            if (AgentContext.getAgentConfig().debug) {
                System.out.println("[TimingAdviceExitVoid] exit " + span.endpoint + " spanId=" + span.spanId);
            }
        } catch (Throwable t) {
            if (AgentContext.getAgentConfig().debug) {
                System.err.println("[TimingAdviceExitVoid] error: " + t.getMessage());
            }
        }
    }
}

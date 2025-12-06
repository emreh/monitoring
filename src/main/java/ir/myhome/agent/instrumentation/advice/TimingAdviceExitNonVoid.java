package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.config.AgentContext;
import ir.myhome.agent.core.Span;
import ir.myhome.agent.core.TraceContextHolder;
import ir.myhome.agent.holder.AgentHolder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

public final class TimingAdviceExitNonVoid {

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void exit(@Advice.Local("spanRef") Span[] spanRef, @Advice.Thrown Throwable thrown,
                            @Advice.Return(readOnly = true, typing = Assigner.Typing.DYNAMIC) Object ret) {
        handleExit(spanRef, thrown, ret);
    }

    private static void handleExit(Span[] spanRef, Throwable thrown, Object ret) {
        try {
            System.out.println("[TimingAdviceExitNonVoid] TimingAdviceExitNonVoid Started");
            if (spanRef == null || spanRef.length == 0) return;

            Span span = spanRef[0];
            if (span == null) return;

            if (thrown != null) span.markError(thrown.getMessage());

            span.end();
            TraceContextHolder.popSpan();

            var q = AgentHolder.getSpanQueue();

            if (q != null) q.offer(span);

            else System.out.println("[TimingAdviceExitNonVoid] fallback span: " + span);

            if (AgentContext.getAgentConfig().debug) {
                System.out.println("[TimingAdviceExitNonVoid] exit " + span.endpoint + " return=" + ret + " spanId=" + span.spanId);
            }
        } catch (Throwable t) {
            if (AgentContext.getAgentConfig().debug) {
                System.err.println("[TimingAdviceExitNonVoid] error: " + t.getMessage());
            }
        }
    }
}

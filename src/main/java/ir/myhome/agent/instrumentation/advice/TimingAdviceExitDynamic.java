package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.config.AgentContext;
import ir.myhome.agent.core.Span;
import ir.myhome.agent.core.TraceContextHolder;
import ir.myhome.agent.holder.AgentHolder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

public final class TimingAdviceExitDynamic {

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void exit(@Advice.Enter Span span, @Advice.Thrown Throwable thrown, @Advice.Return(typing = Assigner.Typing.DYNAMIC) Object ret) {
        try {
            if (span == null) {
                if (AgentContext.getAgentConfig().debug)
                    System.out.println("[TimingAdviceExitDynamic] no span available");
                return;
            }

            if (thrown != null) span.markError(thrown.getMessage());

            span.end();
            Span popped = TraceContextHolder.popSpan();
            if (popped != span && AgentContext.getAgentConfig().debug)
                System.out.println("[TimingAdviceExitDynamic] warning: popped span != enter span");

            var queue = AgentHolder.getSpanQueue();
            if (queue != null) queue.offer(span);
            else System.out.println("[TimingAdviceExitDynamic] fallback span: " + span);

            if (AgentContext.getAgentConfig().debug)
                System.out.println("[TimingAdviceExitDynamic] return=" + ret + " spanId=" + span.spanId);

        } catch (Throwable t) {
            if (AgentContext.getAgentConfig().debug)
                System.err.println("[TimingAdviceExitDynamic] unexpected: " + t.getMessage());
        }
    }
}

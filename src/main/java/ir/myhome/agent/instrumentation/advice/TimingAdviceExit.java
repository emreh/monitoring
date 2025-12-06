package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.config.AgentContext;
import ir.myhome.agent.core.Span;
import ir.myhome.agent.core.TraceContextHolder;
import ir.myhome.agent.holder.AgentHolder;
import net.bytebuddy.asm.Advice;

public final class TimingAdviceExit {

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void exit(@Advice.Local("spanRef") Span[] spanRef, @Advice.Thrown Throwable thrown, @Advice.Return(readOnly = false) Object ret) {
        try {
            if (spanRef == null || spanRef.length == 0) return;

            Span span = spanRef[0];
            if (span == null) return;

            if (thrown != null) {
                try {
                    span.markError(thrown.getMessage());
                } catch (Throwable ignore) {
                }
            }

            try {
                span.end();
            } catch (Throwable ignore) {
            }

            try {
                TraceContextHolder.popSpan();
            } catch (Throwable ignore) {
            }

            try {
                var q = AgentHolder.getSpanQueue();
                if (q != null) {
                    boolean offered = q.offer(span);
                    if (AgentContext.getAgentConfig().debug) {
                        System.out.println("[TimingAdviceExit] offered=" + offered + " spanId=" + span.spanId);
                    }
                } else {
                    // fallback: print safe
                    System.out.println("[TimingAdviceExit] no queue, span: " + span.toString());
                }
            } catch (Throwable t) {
                if (AgentContext.getAgentConfig().debug) {
                    System.err.println("[TimingAdviceExit] publish failed: " + t.getMessage());
                }
            }
        } catch (Throwable outer) {
            if (AgentContext.getAgentConfig().debug) {
                System.err.println("[TimingAdviceExit] unexpected error: " + outer.getMessage());
            }
        }
    }
}

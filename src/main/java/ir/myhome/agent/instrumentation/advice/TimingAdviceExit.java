package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.config.AgentConfig;
import ir.myhome.agent.core.AgentContext;
import ir.myhome.agent.core.Span;
import ir.myhome.agent.core.TraceContextHolder;
import ir.myhome.agent.holder.AgentHolder;
import net.bytebuddy.asm.Advice;

/*
  Exit advice: اگر spanRef null یا طول صفر بود، سریع برمی‌گردیم.
  سپس span را پایان می‌دهیم و آن را به queue می‌فرستیم (یا fallback چاپ).
*/
public final class TimingAdviceExit {

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void exit(@Advice.Local("spanRef") Span[] spanRef, @Advice.Thrown Throwable thrown) {
        try {
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

            try {
                var q = AgentHolder.getSpanQueue();
                if (q != null) {
                    boolean offered = q.offer(span);
                    if (AgentContext.getAgentConfig().debug) {
                        System.out.println("[TimingAdviceExit] offered=" + offered + " spanId=" + span.spanId);
                    }
                } else {
                    // fallback: print (safe, no heavy libs)
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

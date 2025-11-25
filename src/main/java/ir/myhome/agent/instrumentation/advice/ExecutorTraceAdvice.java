package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.context.TraceAwareRunnable;
import ir.myhome.agent.core.TraceContextHolder;
import ir.myhome.agent.core.TraceContextSnapshot;
import net.bytebuddy.asm.Advice;

public final class ExecutorTraceAdvice {

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void wrapRunnable(@Advice.Argument(value = 0, readOnly = false) Runnable r) {
        if (r == null) return;

        TraceContextSnapshot snap = TraceContextHolder.capture();

        r = new TraceAwareRunnable(r, snap);
    }
}

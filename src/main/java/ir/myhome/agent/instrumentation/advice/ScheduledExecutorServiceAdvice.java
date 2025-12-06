package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.context.TraceAwareRunnable;
import ir.myhome.agent.core.TraceContextHolder;
import ir.myhome.agent.core.TraceContextSnapshot;
import net.bytebuddy.asm.Advice;

public final class ScheduledExecutorServiceAdvice {

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void wrap(@Advice.AllArguments(readOnly = false) Object[] args) {
        if (args == null || args.length == 0) return;

        TraceContextSnapshot snap = TraceContextHolder.capture();

        for (int i = 0; i < args.length; i++) {
            Object a = args[i];
            if (a instanceof Runnable) {
                args[i] = new TraceAwareRunnable((Runnable) a, snap);
            }
        }
    }
}

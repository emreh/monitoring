package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.context.TraceAwareCallable;
import ir.myhome.agent.core.TraceContextHolder;
import ir.myhome.agent.core.TraceContextSnapshot;
import net.bytebuddy.asm.Advice;

import java.util.concurrent.Callable;

public final class ExecutorServiceAdvice {

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void wrapCallable(@Advice.Argument(value = 0, readOnly = false) Callable<?> c) {
        if (c == null) return;

        TraceContextSnapshot snap = TraceContextHolder.capture();

        c = new TraceAwareCallable<>(c, snap);
    }
}

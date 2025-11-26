package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.context.TraceAwareCallable;
import ir.myhome.agent.context.TraceAwareRunnable;
import ir.myhome.agent.context.TraceAwareSupplier;
import ir.myhome.agent.core.TraceContextHolder;
import ir.myhome.agent.core.TraceContextSnapshot;
import net.bytebuddy.asm.Advice;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

public final class CompletableFutureAdvice {

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void wrapArgs(@Advice.AllArguments(readOnly = false) Object[] args) {
        if (args == null || args.length == 0) return;

        TraceContextSnapshot snap = TraceContextHolder.capture();

        for (int i = 0; i < args.length; i++) {
            Object a = args[i];

            if (a instanceof Runnable) {
                args[i] = new TraceAwareRunnable((Runnable) a, snap);
            } else if (a instanceof Callable) {
                args[i] = new TraceAwareCallable<>((Callable<?>) a, snap);
            } else if (a instanceof Supplier) {
                args[i] = new TraceAwareSupplier<>((Supplier<?>) a, snap);
            }
        }
    }
}

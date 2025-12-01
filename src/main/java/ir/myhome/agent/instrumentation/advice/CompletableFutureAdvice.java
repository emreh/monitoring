package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.context.TraceAwareSupplier;
import ir.myhome.agent.core.TraceContextHolder;
import ir.myhome.agent.core.TraceContextSnapshot;
import net.bytebuddy.asm.Advice;

import java.util.function.Supplier;

public final class CompletableFutureAdvice {

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void wrapArgs(@Advice.AllArguments(readOnly = false) Object[] args) {
        if (args == null || args.length == 0) return;

        TraceContextSnapshot snap = TraceContextHolder.capture();

        for (int i = 0; i < args.length; i++) {
            Object a = args[i];

            if (a instanceof Supplier) {
                @SuppressWarnings("unchecked") Supplier<?> s = (Supplier<?>) a;
                args[i] = new TraceAwareSupplier<>(s, snap);
            } else if (a instanceof Runnable) {
                // handled by Executor transforms usually
            }
        }
    }
}

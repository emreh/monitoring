package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.context.TraceAwareSupplier;
import ir.myhome.agent.core.TraceContextHolder;
import ir.myhome.agent.core.TraceContextSnapshot;

import java.util.function.Supplier;

/**
 * utility wrapper برای زمانی که callsite بخواهد Supplier را wrap کند.
 * (ما JDK CompletableFuture را instrument نمی‌کنیم تا از crash جلوگیری شود)
 */
public final class CompletableFutureAdvice {

    public static <T> Supplier<T> wrapSupplier(Supplier<T> s) {
        TraceContextSnapshot snap = TraceContextHolder.capture();
        return new TraceAwareSupplier<>(s, snap);
    }
}

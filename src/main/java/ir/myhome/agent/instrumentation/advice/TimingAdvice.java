package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.context.TraceAwareCallable;
import ir.myhome.agent.context.TraceAwareRunnable;
import ir.myhome.agent.core.Span;
import ir.myhome.agent.core.TraceContextHolder;
import ir.myhome.agent.core.TraceContextSnapshot;
import ir.myhome.agent.scheduler.PercentileBatchScheduler;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public final class TimingAdvice {

    @Advice.OnMethodEnter
    public static long onEnter(@Advice.Origin String method) {
        return System.currentTimeMillis();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit(@Advice.Origin String method, @Advice.Enter long start, @Advice.Thrown Throwable throwable, @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object returnValue) {

        long duration = System.currentTimeMillis() - start;
        String traceId = TraceContextHolder.currentTraceId();

        Span span = new Span(traceId, traceId + "-" + System.nanoTime(), null, "service", method, start, duration);
        TraceContextHolder.pushSpan(span);

        PercentileBatchScheduler.record(duration);

        TraceContextSnapshot snapshot = TraceContextHolder.capture();

        // فقط wrap از طریق instrumentation انجام شود، بدون دسترسی مستقیم از controller
        switch (returnValue) {
            case CompletableFuture<?> future ->
                // متد wrapCompletableFutureDeep باقی می‌ماند، استفاده فقط توسط Advice
                    returnValue = wrapCompletableFutureDeepInternal(future, snapshot);
            case Runnable runnable -> returnValue = new TraceAwareRunnable(runnable, snapshot);
            case Callable<?> callable -> returnValue = new TraceAwareCallable<>(callable, snapshot);
            case null, default -> TraceContextHolder.popSpan();
        }

        if (throwable != null) {
            span.markError(throwable.getMessage());
        }
    }

    // متد private و internal، فقط توسط Advice استفاده می‌شود
    @SuppressWarnings("unchecked")
    public static <T> CompletableFuture<T> wrapCompletableFutureDeepInternal(CompletableFuture<T> future, TraceContextSnapshot snapshot) {
        CompletableFuture<T> wrapped = new CompletableFuture<>();

        future.whenComplete((result, throwable) -> {
            TraceContextSnapshot prev = TraceContextHolder.restore(snapshot);
            try {
                if (throwable != null) {
                    wrapped.completeExceptionally(throwable);
                } else {
                    wrapped.complete(result);
                }
            } finally {
                TraceContextHolder.restore(prev);
            }
        });

        return (CompletableFuture<T>) wrapped.thenApply(wrapFunction(snapshot)).thenApplyAsync(wrapFunction(snapshot)).thenCompose(wrapCompose(snapshot)).thenAccept(wrapConsumer(snapshot)).thenAcceptAsync(wrapConsumer(snapshot)).exceptionally(ex -> {
            TraceContextHolder.restore(snapshot);
            throw new RuntimeException(ex);
        });
    }

    private static <T, R> Function<T, R> wrapFunction(TraceContextSnapshot snapshot) {
        return t -> {
            TraceContextSnapshot prev = TraceContextHolder.restore(snapshot);
            try {
                return (R) t;
            } finally {
                TraceContextHolder.restore(prev);
            }
        };
    }

    private static <T> Consumer<T> wrapConsumer(TraceContextSnapshot snapshot) {
        return t -> {
            TraceContextSnapshot prev = TraceContextHolder.restore(snapshot);
            try {
                // no-op
            } finally {
                TraceContextHolder.restore(prev);
            }
        };
    }

    private static <T> Function<T, CompletableFuture<T>> wrapCompose(TraceContextSnapshot snapshot) {
        return t -> {
            TraceContextSnapshot prev = TraceContextHolder.restore(snapshot);
            try {
                return CompletableFuture.completedFuture(t);
            } finally {
                TraceContextHolder.restore(prev);
            }
        };
    }
}

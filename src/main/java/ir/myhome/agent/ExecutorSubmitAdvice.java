package ir.myhome.agent;

import net.bytebuddy.asm.Advice;

import java.util.concurrent.Callable;

public class ExecutorSubmitAdvice {

    // submit(Runnable) -> replace arg0
    @Advice.OnMethodEnter
    public static void onEnterRunnable(@Advice.Argument(value = 0, readOnly = false) Runnable task) {
        if (task == null) return;
        String traceId = TraceContext.getTraceId();
        // wrap only if not already wrapped (quick guard)
        if (!(task instanceof TraceableRunnable)) {
            task = new TraceableRunnable(task, traceId);
        }
    }

    // submit(Callable) -> replace arg0
    @Advice.OnMethodEnter
    public static void onEnterCallable(@Advice.Argument(value = 0, readOnly = false) Callable<?> task) {
        if (task == null) return;
        String traceId = TraceContext.getTraceId();
        if (!(task instanceof TraceableCallable)) {
            task = new TraceableCallable<>(task, traceId);
        }
    }

    // submit(Runnable, result) -> replace arg0 (the runnable)
    @Advice.OnMethodEnter
    public static void onEnterRunnableWithResult(@Advice.Argument(value = 0, readOnly = false) Runnable task,
                                                 @Advice.Argument(value = 1, readOnly = true) Object result) {
        if (task == null) return;
        String traceId = TraceContext.getTraceId();
        if (!(task instanceof TraceableRunnable)) {
            task = new TraceableRunnable(task, traceId);
        }
    }
}

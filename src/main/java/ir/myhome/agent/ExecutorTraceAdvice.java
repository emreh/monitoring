package ir.myhome.agent;

import net.bytebuddy.asm.Advice;

import java.util.concurrent.Callable;

public class ExecutorTraceAdvice {

    @Advice.OnMethodEnter
    static void onEnter(@Advice.Argument(value = 0, readOnly = false) Object task) {
        // Capture parent thread's traceId
        String traceId = TraceContext.getTraceId();

        if (task instanceof Runnable original) {
            task = (Runnable) () -> {
                TraceContext.setTraceId(traceId);
                try {
                    original.run();
                } finally {
                    TraceContext.clear();
                }
            };
        } else if (task instanceof Callable<?> original) {
            task = (Callable<Object>) () -> {
                TraceContext.setTraceId(traceId);
                try {
                    return original.call();
                } finally {
                    TraceContext.clear();
                }
            };
        }
    }
}

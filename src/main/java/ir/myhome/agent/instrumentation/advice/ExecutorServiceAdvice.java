package ir.myhome.agent.instrumentation.advice;

import net.bytebuddy.asm.Advice;

public class ExecutorServiceAdvice {

    @Advice.OnMethodEnter
    public static void onEnter(@Advice.Argument(value = 0, readOnly = false) Object task) {
        // reuse ExecutorTraceAdvice logic
        ExecutorTraceAdvice.onEnter(task);
    }
}


package ir.myhome.agent.instrumentation;

import ir.myhome.agent.core.SpanExporter;
import ir.myhome.agent.core.TraceState;
import net.bytebuddy.asm.Advice;

import java.util.concurrent.Callable;

public class ExecutorTraceAdvice {

    // این شیء را AgentMain تنظیم می‌کند
    public static volatile SpanExporter exporter;

    public static void setExporter(SpanExporter e) {
        exporter = e;
    }

    @Advice.OnMethodEnter
    public static void onEnter(@Advice.Argument(value = 0, readOnly = false) Object task) {

        String traceId = TraceState.getTraceId();
        String parentSpan = TraceState.peekSpan();

        if (task instanceof Runnable && !(task instanceof ir.myhome.agent.TraceAwareRunnable)) {
            task = new ir.myhome.agent.TraceAwareRunnable((Runnable) task, traceId, parentSpan);
        } else if (task instanceof Callable && !(task instanceof ir.myhome.agent.TraceAwareCallable)) {
            task = new ir.myhome.agent.TraceAwareCallable<>((Callable<?>) task, traceId, parentSpan);
        }
    }
}

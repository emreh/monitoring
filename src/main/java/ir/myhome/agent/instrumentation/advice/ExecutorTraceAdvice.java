package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.core.SpanExporter;
import ir.myhome.agent.core.TraceState;
import net.bytebuddy.asm.Advice;

import java.util.concurrent.Callable;

public class ExecutorTraceAdvice {

    // exporter is injected by AgentMain
    public static volatile SpanExporter exporter;

    public static void setExporter(SpanExporter e) {
        exporter = e;
    }

    // advice that wraps the first argument (Runnable/Callable) before submit/execute
    @Advice.OnMethodEnter
    public static void onEnter(@Advice.Argument(value = 0, readOnly = false) Object task) {
        String traceId = TraceState.getTraceId();
        String parentSpan = TraceState.peekSpan();

        if (task instanceof Runnable && !(task instanceof ir.myhome.agent.TraceAwareRunnable)) {
            task = new ir.myhome.agent.TraceAwareRunnable((Runnable) task, traceId, parentSpan);
        } else if (task instanceof Callable && !(task instanceof ir.myhome.agent.TraceAwareCallable)) {
            task = new ir.myhome.agent.TraceAwareCallable<>((Callable<?>) task, traceId, parentSpan);
        }

        // ByteBuddy will set the argument to this new wrapper (readOnly=false)
    }
}

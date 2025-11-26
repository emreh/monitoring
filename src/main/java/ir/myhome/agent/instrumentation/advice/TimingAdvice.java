package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.core.Span;
import net.bytebuddy.asm.Advice;

public final class TimingAdvice {

    @Advice.OnMethodEnter
    public static Span enter(@Advice.Origin String method) {
        Span span = new Span();
        System.out.println("[TimingAdvice] enter " + method + " spanId=" + span.traceId);
        return span;
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void exit(@Advice.Enter Span span, @Advice.Origin String method) {
        span.finish();
        System.out.println("[TimingAdvice] exit " + method + " duration=" + (span.endTime - span.startTime) + "ms");
    }
}

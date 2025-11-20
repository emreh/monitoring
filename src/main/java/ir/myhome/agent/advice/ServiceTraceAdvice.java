package ir.myhome.agent.advice;

import ir.myhome.agent.TraceContext;
import net.bytebuddy.asm.Advice;

import java.util.UUID;

public class ServiceTraceAdvice {

    @Advice.OnMethodEnter
    static void onEnter() {
        if (TraceContext.getTraceId() == null) {
            TraceContext.setTraceId(UUID.randomUUID().toString());
        }
        System.out.println("TRACE_ENTER=" + TraceContext.getTraceId());
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    static void onExit(@Advice.Origin("#t.#m") String method) {
        String traceId = TraceContext.getTraceId();
        String spanId = UUID.randomUUID().toString();
        System.out.println("TRACE_EXIT traceId=" + traceId + ", spanId=" + spanId + ", method=" + method + ", status=SUCCESS");
    }
}

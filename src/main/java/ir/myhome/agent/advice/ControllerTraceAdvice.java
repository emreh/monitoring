package ir.myhome.agent.advice;

import net.bytebuddy.asm.Advice;

import java.util.UUID;

public class ControllerTraceAdvice {

    @Advice.OnMethodEnter
    public static void onEnter(@Advice.Origin("#t.#m") String methodName) {
        String traceId = TraceContext.getTraceId();
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
            TraceContext.setTraceId(traceId);
        }

        System.out.println("[Controller Enter] traceId=" + traceId + " method=" + methodName);
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit(@Advice.Origin("#t.#m") String methodName,
                              @Advice.Thrown Throwable thrown) {

        String traceId = TraceContext.getTraceId();

        if (thrown == null) {
            System.out.println("[Controller Exit] traceId=" + traceId + " method=" + methodName);
        } else {
            System.out.println("[Controller Exception] traceId=" + traceId + " method=" +
                    methodName + " ex=" + thrown);
        }

        TraceContext.clear();
    }
}

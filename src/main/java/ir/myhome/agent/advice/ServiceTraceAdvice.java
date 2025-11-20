package ir.myhome.agent.advice;

import net.bytebuddy.asm.Advice;

import java.util.UUID;

public class ServiceTraceAdvice {

    @Advice.OnMethodEnter
    public static long onEnter(@Advice.Origin("#t.#m") String methodName) {

        String traceId = TraceContext.getTraceId();
        String spanId = UUID.randomUUID().toString();

        System.out.println("[Service Enter] traceId=" + traceId +
                " spanId=" + spanId +
                " method=" + methodName);

        return System.nanoTime();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit(@Advice.Origin("#t.#m") String methodName,
                              @Advice.Enter long startTime,
                              @Advice.Thrown Throwable thrown) {

        long duration = System.nanoTime() - startTime;
        String traceId = TraceContext.getTraceId();

        if (thrown == null) {
            System.out.println("[Service Exit] traceId=" + traceId +
                    " method=" + methodName +
                    " durationNs=" + duration);
        } else {
            System.out.println("[Service Exception] traceId=" + traceId +
                    " method=" + methodName +
                    " durationNs=" + duration +
                    " ex=" + thrown);
        }
    }
}

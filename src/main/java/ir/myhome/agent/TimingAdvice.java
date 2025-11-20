package ir.myhome.agent;

import net.bytebuddy.asm.Advice;

import java.util.UUID;

public class TimingAdvice {

    @Advice.OnMethodEnter
    static void onEnter() {
        if (TraceContext.getTraceId() == null) {
            TraceContext.setTraceId(UUID.randomUUID().toString());
        }
        System.out.println("TRACE_ENTER=" + TraceContext.getTraceId());
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    static void onExit(@Advice.Origin("#t.#m") String method,
                       @Advice.Thrown Throwable thrown) {
        String traceId = TraceContext.getTraceId();
        if (traceId == null) traceId = UUID.randomUUID().toString();

        String status = (thrown == null) ? "SUCCESS" : "ERROR";
        String spanId = UUID.randomUUID().toString();

        System.out.printf("TRACE_EXIT traceId=%s, spanId=%s, method=%s, status=%s%n",
                traceId, spanId, method, status);

        // enqueue JSON if needed
        String json = String.format("{\"traceId\":\"%s\",\"spanId\":\"%s\",\"endpoint\":\"%s\",\"status\":\"%s\"}",
                traceId, spanId, method, status);
        AgentMain.enqueueSpan(json);
    }
}

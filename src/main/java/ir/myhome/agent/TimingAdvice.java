package ir.myhome.agent;

import net.bytebuddy.asm.Advice;

import java.util.UUID;

public class TimingAdvice {

    @Advice.OnMethodEnter
    static long onEnter() {
        return System.nanoTime();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    static void onExit(@Advice.Origin("#t.#m") String method,
                       @Advice.Enter long startTime,
                       @Advice.Thrown Throwable thrown) {
        try {
            long durationMs = (System.nanoTime() - startTime) / 1_000_000L;
            String traceId = TraceContext.get();
            if (traceId == null)
                traceId = UUID.randomUUID().toString();

            String spanId = UUID.randomUUID().toString();
            String status = (thrown == null) ? "SUCCESS" : "ERROR";

            String json = String.format(
                    "{\"traceId\":\"%s\",\"spanId\":\"%s\",\"parentId\":null,\"service\":\"%s\",\"endpoint\":\"%s\",\"startEpochMs\":%d,\"durationMs\":%d,\"status\":\"%s\"}",
                    traceId,
                    spanId,
                    "unknown-service",
                    escapeJson(method),
                    System.currentTimeMillis(),
                    durationMs,
                    status
            );

            AgentMain.enqueueSpan(json);
        } catch (Throwable t) {
            // swallow
        }
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

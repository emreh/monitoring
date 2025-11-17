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
            String traceId = UUID.randomUUID().toString();
            String spanId = UUID.randomUUID().toString();
            String status = (thrown == null) ? "SUCCESS" : "ERROR";

            // build JSON minimal payload (escape method if needed)
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
            // swallow everything to avoid impacting app
        }
    }

    // very small JSON escaper for double quotes and backslash
    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

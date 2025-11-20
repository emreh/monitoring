package ir.myhome.agent;

import net.bytebuddy.asm.Advice;

/**
 * Minimal timing advice: prints enter/exit and duration in ms.
 * Uses System.out to avoid logging framework conflicts.
 */
public class MethodTimerAdvice {

    @Advice.OnMethodEnter
    public static long onEnter(@Advice.Origin("#t.#m") String method) {
        long start = System.nanoTime();
        System.out.println("[AGENT] Enter: " + method + " thread=" + Thread.currentThread().getName());
        return start;
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit(@Advice.Origin("#t.#m") String method,
                              @Advice.Enter long start,
                              @Advice.Thrown Throwable thrown) {
        long tookNs = System.nanoTime() - start;
        long tookMs = tookNs / 1_000_000L;
        String status = (thrown == null) ? "OK" : "ERROR";
        System.out.println("[AGENT] Exit : " + method + " took=" + tookMs + "ms status=" + status);
        if (thrown != null) {
            System.out.println("[AGENT]   thrown: " + thrown.getClass().getName() + " : " + thrown.getMessage());
        }
    }
}

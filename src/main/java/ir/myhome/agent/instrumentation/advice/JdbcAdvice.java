package ir.myhome.agent.instrumentation.advice;

import net.bytebuddy.asm.Advice;

import java.util.concurrent.atomic.AtomicLong;

public final class JdbcAdvice {

    private static final AtomicLong counter = new AtomicLong();

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static long enter() {
        return System.nanoTime();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void exit(@Advice.Enter long start) {
        long duration = System.nanoTime() - start;
        long count = counter.incrementAndGet();
        System.out.println("[JdbcAdvice] JDBC call #" + count + " took " + (duration / 1_000_000.0) + " ms");
    }
}

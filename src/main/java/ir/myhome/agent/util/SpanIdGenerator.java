package ir.myhome.agent.util;

import java.util.concurrent.atomic.AtomicLong;

public final class SpanIdGenerator {
    private static final AtomicLong COUNTER = new AtomicLong(System.nanoTime());

    private SpanIdGenerator() {
    }

    public static String nextId() {
        long v = COUNTER.incrementAndGet();
        int th = System.identityHashCode(Thread.currentThread());

        return Long.toHexString(v) + "-" + Integer.toHexString(th);
    }
}

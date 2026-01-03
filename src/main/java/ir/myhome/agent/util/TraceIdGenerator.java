package ir.myhome.agent.util;

import java.util.concurrent.atomic.AtomicLong;

public final class TraceIdGenerator {

    private static final AtomicLong COUNTER = new AtomicLong(System.nanoTime());

    private TraceIdGenerator() {
    }

    public static long nextId() {
        return COUNTER.incrementAndGet();
    }
}

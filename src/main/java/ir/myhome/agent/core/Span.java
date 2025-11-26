package ir.myhome.agent.core;

import java.util.UUID;

public final class Span {
    public final String traceId;
    public final long startTime;
    public long endTime;

    public Span() {
        this.traceId = UUID.randomUUID().toString();
        this.startTime = System.currentTimeMillis();
    }

    public void finish() {
        this.endTime = System.currentTimeMillis();
    }
}

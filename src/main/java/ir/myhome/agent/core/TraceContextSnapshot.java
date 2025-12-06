package ir.myhome.agent.core;

import java.util.ArrayDeque;
import java.util.Deque;

public final class TraceContextSnapshot {
    private final Deque<Span> stack;

    public TraceContextSnapshot(Deque<Span> stack) {
        this.stack = stack != null ? stack : new ArrayDeque<>();
    }

    public Deque<Span> getStack() {
        return new ArrayDeque<>(stack); // defensive copy
    }
}

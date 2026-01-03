package ir.myhome.agent.core;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Thread-local stack of spans
 * Supports capture/restore for async tasks
 */
public final class TraceContextHolder {

    private static final ThreadLocal<Deque<Span>> SPAN_STACK = ThreadLocal.withInitial(ArrayDeque::new);

    private TraceContextHolder() {}

    /** Push a span onto the thread-local stack */
    public static void pushSpan(Span span) {
        if (span != null) SPAN_STACK.get().push(span);
    }

    /** Pop the top span from the thread-local stack */
    public static Span popSpan() {
        Deque<Span> stack = SPAN_STACK.get();
        return stack.isEmpty() ? null : stack.pop();
    }

    /** Peek at the top span */
    public static Span currentSpan() {
        Deque<Span> stack = SPAN_STACK.get();
        return stack.isEmpty() ? null : stack.peek();
    }

    /** Get current traceId */
    public static Long currentTraceId() {
        Span span = currentSpan();
        return span != null ? span.traceId : null;
    }

    /** Get current spanId */
    public static String currentSpanId() {
        Span span = currentSpan();
        return span != null ? span.spanId : null;
    }

    /** Capture snapshot for async tasks */
    public static TraceContextSnapshot capture() {
        Deque<Span> stack = SPAN_STACK.get();
        return new TraceContextSnapshot(new ArrayDeque<>(stack));
    }

    /** Restore snapshot; returns previous snapshot */
    public static TraceContextSnapshot restore(TraceContextSnapshot snapshot) {
        Deque<Span> prev = SPAN_STACK.get();
        SPAN_STACK.set(snapshot != null ? new ArrayDeque<>(snapshot.getStack()) : new ArrayDeque<>());
        return new TraceContextSnapshot(prev);
    }
}

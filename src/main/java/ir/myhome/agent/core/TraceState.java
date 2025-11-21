package ir.myhome.agent.core;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

public final class TraceState {

    private static final ThreadLocal<String> traceIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<Deque<String>> spanStack = ThreadLocal.withInitial(ArrayDeque::new);

    private TraceState() {
    }

    // traceId APIs
    public static void clearTraceId() {
        traceIdHolder.remove();
    }

    public static String ensureTraceId() {
        String id = traceIdHolder.get();
        if (id == null) {
            id = UUID.randomUUID().toString();
            traceIdHolder.set(id);
        }
        return id;
    }

    public static String getTraceId() {
        return traceIdHolder.get();
    }

    public static void setTraceId(String id) {
        if (id == null) traceIdHolder.remove();
        else traceIdHolder.set(id);
    }

    // span stack APIs
    public static void pushSpan(String spanId) {
        if (spanId == null) return;
        spanStack.get().push(spanId);
    }

    public static String popSpan() {
        Deque<String> dq = spanStack.get();
        if (dq.isEmpty()) return null;
        return dq.pop();
    }

    public static String peekSpan() {
        Deque<String> dq = spanStack.get();
        return dq.isEmpty() ? null : dq.peek();
    }

    public static void clearSpans() {
        spanStack.remove();
    }
}

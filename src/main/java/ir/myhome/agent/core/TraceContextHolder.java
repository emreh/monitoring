package ir.myhome.agent.core;

import java.util.ArrayDeque;
import java.util.Deque;

public final class TraceContextHolder {

    private static final ThreadLocal<LocalCtx> LOCAL = ThreadLocal.withInitial(LocalCtx::new);

    private static final class LocalCtx {
        String traceId;
        Deque<String> spanStack = new ArrayDeque<>(8);
    }

    private TraceContextHolder() {
    }

    public static void pushSpan(String spanId, String traceId) {
        if (spanId == null) return;

        LocalCtx c = LOCAL.get();

        if (c.traceId == null && traceId != null) c.traceId = traceId;

        c.spanStack.push(spanId);
    }

    public static String popSpan() {
        LocalCtx c = LOCAL.get();

        if (c == null) return null;

        String v = c.spanStack.isEmpty() ? null : c.spanStack.pop();

        if (c.spanStack.isEmpty()) LOCAL.remove();

        return v;
    }

    public static String currentSpanId() {
        LocalCtx c = LOCAL.get();
        return (c == null) ? null : c.spanStack.peek();
    }

    public static String currentTraceId() {
        LocalCtx c = LOCAL.get();

        if (c == null) return null;

        if (c.traceId == null) c.traceId = generateTraceId();

        return c.traceId;
    }

    private static String generateTraceId() {
        // cheap, bootstrap-safe trace id (avoid SecureRandom/UUID$Holder)
        return Long.toHexString(System.nanoTime()) + "-" + Integer.toHexString(System.identityHashCode(Thread.currentThread()));
    }

    public static TraceContextSnapshot capture() {
        LocalCtx c = LOCAL.get();

        if (c == null || c.spanStack.isEmpty()) return TraceContextSnapshot.EMPTY;

        String[] arr = c.spanStack.toArray(new String[0]);

        return new TraceContextSnapshot(c.traceId, arr);
    }

    public static TraceContextSnapshot restore(TraceContextSnapshot snapshot) {
        LocalCtx prev = LOCAL.get();
        TraceContextSnapshot prevSnap;

        if (prev == null || prev.spanStack.isEmpty()) prevSnap = TraceContextSnapshot.EMPTY;
        else prevSnap = new TraceContextSnapshot(prev.traceId, prev.spanStack.toArray(new String[0]));

        if (snapshot == null || snapshot == TraceContextSnapshot.EMPTY) {
            LOCAL.remove();
            return prevSnap;
        }

        LocalCtx newCtx = new LocalCtx();
        newCtx.traceId = snapshot.traceId;

        for (int i = snapshot.spanStack.length - 1; i >= 0; i--) newCtx.spanStack.push(snapshot.spanStack[i]);

        LOCAL.set(newCtx);
        return prevSnap;
    }

    public static void clear() {
        LOCAL.remove();
    }
}

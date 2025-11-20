package ir.myhome.agent;

public final class TraceContext {

    private static final ThreadLocal<String> TRACE_ID = new ThreadLocal<>();

    private TraceContext() {
    }

    public static void set(String traceId) {
        TRACE_ID.set(traceId);
    }

    public static String get() {
        return TRACE_ID.get();
    }

    public static String getOrCreate() {
        String t = TRACE_ID.get();
        if (t == null) {
            t = java.util.UUID.randomUUID().toString();
            TRACE_ID.set(t);
        }
        return t;
    }

    public static void clear() {
        TRACE_ID.remove();
    }
}

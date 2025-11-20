package ir.myhome.agent;

import java.util.UUID;

public final class TraceContext {

    private static final ThreadLocal<String> traceIdHolder = new ThreadLocal<>();

    //آزاد کردن ThreadLocal ضروری است (به‌خصوص ThreadPool).
    public static void clear() {
        traceIdHolder.remove();
    }

    //همیشه یک TraceId ساختارمند می‌دهد
    public static String ensure() {
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
        traceIdHolder.set(id);
    }
}

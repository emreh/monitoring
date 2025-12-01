package ir.myhome.agent.core;

import java.util.UUID;

public final class TraceContext {

    private TraceContext() {
    }

    public static String newId() {
        return UUID.randomUUID().toString();
    }

    public static Span newRootSpan(String service, String endpoint) {
        String t = newId();
        String s = newId();
        return new Span(t, s, null, service, endpoint, System.currentTimeMillis());
    }
}

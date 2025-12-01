package ir.myhome.agent.core;

import java.util.Map;
import java.util.StringJoiner;

public final class JsonSerializer {

    private JsonSerializer() {
    }

    public static String toJson(Span s) {
        if (s == null) return "{}";
        StringJoiner sj = new StringJoiner(",", "{", "}");
        addKV(sj, "traceId", s.traceId);
        addKV(sj, "spanId", s.spanId);
        addKV(sj, "parentId", s.parentId);
        addKV(sj, "service", s.service);
        addKV(sj, "endpoint", s.endpoint);
        addKV(sj, "startEpochMs", Long.toString(s.startEpochMs));
        addKV(sj, "durationMs", Long.toString(s.durationMs));
        addKV(sj, "status", s.status);

        if (s.errorMessage != null) addKV(sj, "errorMessage", s.errorMessage);

        // tags
        if (s.tags != null && !s.tags.isEmpty()) {
            StringJoiner tags = new StringJoiner(",", "{", "}");

            for (Map.Entry<String, String> e : s.tags.entrySet()) {
                tags.add("\"" + escape(e.getKey()) + "\":\"" + escape(e.getValue()) + "\"");
            }

            sj.add("\"tags\":" + tags.toString());
        }
        return sj.toString();
    }

    private static void addKV(StringJoiner sj, String k, String v) {
        sj.add("\"" + escape(k) + "\":\"" + escape(v) + "\"");
    }

    private static String escape(String s) {
        if (s == null) return "";

        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}

package ir.myhome.agent.exporter;

import ir.myhome.agent.core.Span;

public final class JsonSerializer {

    private JsonSerializer() {
    }

    public static String toJson(Span s) {
        if (s == null) return "{}";
        StringBuilder sb = new StringBuilder();
        sb.append("{")
                .append("\"traceId\":\"").append(escape(s.traceId)).append("\",")
                .append("\"startTime\":").append(s.startTime).append(",")
                .append("\"endTime\":").append(s.endTime)
                .append("}");
        return sb.toString();
    }

    private static String escape(String str) {
        return str.replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}

package ir.myhome.agent.core;

public final class JsonSerializer {

    public static String toJson(Span s) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        append(sb, "traceId", s.getTraceId());
        append(sb, "spanId", s.getSpanId());
        append(sb, "parentId", s.getParentId());
        append(sb, "service", s.getService());
        append(sb, "endpoint", s.getEndpoint());
        sb.append("\"startEpochMs\":").append(s.startEpochMs).append(",");
        sb.append("\"durationMs\":").append(s.durationMs).append(",");
        append(sb, "status", s.status);

        if (s.errorMessage != null) append(sb, "errorMessage", s.errorMessage);

        if (sb.charAt(sb.length() - 1) == ',') sb.setLength(sb.length() - 1);

        sb.append("}");
        return sb.toString();
    }

    private static void append(StringBuilder sb, String key, String val) {
        if (val == null) return;
        sb.append("\"").append(key).append("\":\"").append(escape(val)).append("\",");
    }

    private static String escape(String s) {
        if (s == null) return "";

        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}

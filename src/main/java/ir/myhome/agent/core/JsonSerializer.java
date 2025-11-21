package ir.myhome.agent.core;

public final class JsonSerializer {

    public static String toJson(Span s) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"traceId\":\"").append(escape(s.traceId)).append("\",");
        sb.append("\"spanId\":\"").append(escape(s.spanId)).append("\",");
        sb.append("\"parentId\":").append(s.parentId == null ? "null" : ("\"" + escape(s.parentId) + "\"")).append(",");
        sb.append("\"service\":\"").append(escape(s.service)).append("\",");
        sb.append("\"endpoint\":\"").append(escape(s.endpoint)).append("\",");
        sb.append("\"startEpochMs\":").append(s.startEpochMs).append(",");
        sb.append("\"durationMs\":").append(s.durationMs).append(",");
        sb.append("\"status\":\"").append(escape(s.status)).append("\"");
        sb.append("}");
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}

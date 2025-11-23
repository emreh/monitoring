package ir.myhome.agent.core;

public final class JsonSerializer {

    public static String toJson(Span s) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        append(sb, "traceId", s.traceId);
        append(sb, "spanId", s.spanId);
        append(sb, "parentId", s.parentId);
        append(sb, "service", s.service);
        append(sb, "endpoint", s.endpoint);

        sb.append("\"startEpochMs\":").append(s.startEpochMs).append(",");
        sb.append("\"durationMs\":").append(s.durationMs).append(",");
        append(sb, "status", s.status);

        // جدید: پیام خطا
        if (s.errorMessage != null) {
            append(sb, "errorMessage", s.errorMessage);
        }

        // آخرین کاما را حذف کنیم اگر بود
        if (sb.charAt(sb.length() - 1) == ',') sb.setLength(sb.length() - 1);

        sb.append("}");
        return sb.toString();
    }

    private static void append(StringBuilder sb, String key, String value) {
        if (value == null) return;

        sb.append("\"").append(key).append("\":\"").append(escape(value)).append("\",");
    }

    private static String escape(String s) {
        if (s == null) return "";

        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}

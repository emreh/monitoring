// file: src/main/java/ir/myhome/agent/util/PrettyArgRenderer.java
package ir.myhome.agent.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.StringJoiner;

public final class PrettyArgRenderer {

    public static final int MAX_STRING = 200;
    public static final int MAX_COLLECTION_SIZE = 20;
    public static final int MAX_MAP_ENTRIES = 20;

    private PrettyArgRenderer() {
    }

    public static String render(Object o) {
        if (o == null) return "null";

        try {

            if (o instanceof String) return quote(truncate((String) o));

            if (o instanceof Number || o instanceof Boolean || o instanceof Character) return String.valueOf(o);

            if (o instanceof Collection) return renderCollection((Collection<?>) o);

            if (o instanceof Map) return renderMap((Map<?, ?>) o);

            if (o.getClass().isArray()) return renderArray(o);

            String cls = o.getClass().getSimpleName();
            String t = safeToString(o);

            if (t != null && t.length() < 300 && !t.contains("@")) return cls + "(" + truncate(t) + ")";

            return cls + "@" + Integer.toHexString(System.identityHashCode(o));
        } catch (Throwable tt) {
            return "<render-error>";
        }
    }

    private static String renderCollection(Collection<?> c) {
        int size = c.size();
        String prefix = "list(size=" + size + ")";
        if (size == 0) return prefix;
        StringJoiner j = new StringJoiner(", ", "[", "]");
        int i = 0;
        for (Object e : c) {
            if (i++ >= MAX_COLLECTION_SIZE) {
                j.add("...");
                break;
            }
            j.add(renderShallow(e));
        }
        return prefix + j.toString();
    }

    private static String renderMap(Map<?, ?> m) {
        int size = m.size();
        String prefix = "map(size=" + size + ")";

        if (size == 0) return prefix;

        StringJoiner j = new StringJoiner(", ", "{", "}");
        int i = 0;

        for (Map.Entry<?, ?> en : m.entrySet()) {
            if (i++ >= MAX_MAP_ENTRIES) {
                j.add("...");
                break;
            }
            j.add(renderShallow(en.getKey()) + ":" + renderShallow(en.getValue()));
        }
        return prefix + j.toString();
    }

    private static String renderArray(Object a) {
        int len = Array.getLength(a);
        String comp = a.getClass().getComponentType() != null ? a.getClass().getComponentType().getSimpleName() : "Object";
        return comp + "[](" + len + ")";
    }

    private static String renderShallow(Object e) {
        if (e == null) return "null";

        if (e instanceof String) return quote(truncate((String) e));

        if (e instanceof Number || e instanceof Boolean || e instanceof Character) return String.valueOf(e);

        String cls = e.getClass().getSimpleName();
        String t = safeToString(e);

        if (t != null && t.length() < 80 && !t.contains("@")) return cls + "(" + truncate(t) + ")";

        return cls + "@" + Integer.toHexString(System.identityHashCode(e));
    }

    private static String truncate(String s) {
        if (s == null) return "null";

        if (s.length() <= MAX_STRING) return s;

        return s.substring(0, MAX_STRING) + "...";
    }

    private static String quote(String s) {
        return "\"" + s.replace("\"", "\\\"") + "\"";
    }

    private static String safeToString(Object o) {
        try {
            return o.toString();
        } catch (Throwable t) {
            return null;
        }
    }
}

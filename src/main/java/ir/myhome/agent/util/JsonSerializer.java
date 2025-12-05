// file: src/main/java/ir/myhome/agent/util/JsonSerializer.java
package ir.myhome.agent.util;

import tools.jackson.databind.ObjectMapper;

/**
  lazy-init ObjectMapper to avoid heavy classloading in Advice.
  (ObjectMapper class will only be loaded in worker/exporter threads.)
*/
public final class JsonSerializer {
    private JsonSerializer() {
    }

    private static class Holder {
        static final ObjectMapper MAPPER = create();

        private static ObjectMapper create() {
            try {
                return new ObjectMapper();
            } catch (Throwable t) {
                return null;
            }
        }
    }

    public static String toJson(Object o) {
        try {
            ObjectMapper m = Holder.MAPPER;

            if (m == null) return "\"<no-mapper>\"";

            return m.writeValueAsString(o);
        } catch (Throwable t) {
            return "\"<unserializable>\"";
        }
    }
}

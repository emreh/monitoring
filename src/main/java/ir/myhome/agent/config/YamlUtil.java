package ir.myhome.agent.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class YamlUtil {

    public static AgentConfig loadAgentConfig(String resourceName) {
        AgentConfig cfg = new AgentConfig();
        try (InputStream in = YamlUtil.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) return cfg;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String line;
                String top = null;
                String second = null;
                while ((line = br.readLine()) != null) {
                    line = line.split("#", 2)[0];
                    if (line.trim().isEmpty()) continue;
                    if (!line.startsWith(" ") && line.trim().endsWith(":")) {
                        top = line.trim().replace(":", "");
                        second = null;
                        continue;
                    }
                    if (line.startsWith("  ") && line.trim().endsWith(":")) {
                        second = line.trim().replace(":", "");
                        continue;
                    }
                    String trimmed = line.trim();
                    String[] kv = trimmed.contains(":") ? trimmed.split(":", 2) : trimmed.split("=", 2);
                    if (kv.length != 2) continue;
                    String key = kv[0].trim();
                    String val = kv[1].trim().replaceAll("^\"|\"$", "");

                    if ("agent".equals(top) && "exporter".equals(second)) {
                        switch (key) {
                            case "type" -> cfg.exporterType = val;
                            case "batchSize" -> {
                                try {
                                    cfg.exporterBatchSize = Integer.parseInt(val);
                                } catch (Exception ignored) {
                                }
                            }
                            case "capacity" -> {
                                try {
                                    cfg.exporterCapacity = Integer.parseInt(val);
                                } catch (Exception ignored) {
                                }
                            }
                            case "endpoint" -> cfg.exporterEndpoint = val;
                        }
                    } else if ("instrumentation".equals(top)) {
                        switch (key) {
                            case "app" -> cfg.instrumentationApp = parseBool(val, cfg.instrumentationApp);
                            case "executor" ->
                                    cfg.instrumentationExecutor = parseBool(val, cfg.instrumentationExecutor);
                            case "httpClient" ->
                                    cfg.instrumentationHttpClient = parseBool(val, cfg.instrumentationHttpClient);
                            case "jdbc" -> cfg.instrumentationJdbc = parseBool(val, cfg.instrumentationJdbc);
                            case "scheduled" ->
                                    cfg.instrumentationScheduled = parseBool(val, cfg.instrumentationScheduled);
                            case "reactive" ->
                                    cfg.instrumentationReactive = parseBool(val, cfg.instrumentationReactive);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            System.err.println("[YamlUtil] failed to load " + resourceName + " : " + t.getMessage());
        }
        return cfg;
    }

    private static boolean parseBool(String v, boolean def) {
        if (v == null || v.isBlank()) return def;
        v = v.toLowerCase();
        return v.equals("1") || v.equals("true") || v.equals("yes");
    }
}

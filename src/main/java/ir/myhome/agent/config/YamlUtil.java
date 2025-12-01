package ir.myhome.agent.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class YamlUtil {

    public static AgentConfig loadAgentConfig(String resourceName) {
        AgentConfig cfg = new AgentConfig();

        InputStream in = null;
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();

            if (cl != null) in = cl.getResourceAsStream(resourceName);

            if (in == null) in = YamlUtil.class.getClassLoader().getResourceAsStream(resourceName);

            if (in == null) return cfg;

            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String line;
                String currentTop = null;
                String currentSecond = null;

                while ((line = br.readLine()) != null) {
                    line = line.split("#", 2)[0];

                    if (line.trim().isEmpty()) continue;

                    if (!line.startsWith(" ") && line.trim().endsWith(":")) {
                        currentTop = line.trim().replace(":", "");
                        currentSecond = null;
                        continue;
                    }

                    if (line.startsWith("  ") && line.trim().endsWith(":")) {
                        currentSecond = line.trim().replace(":", "");
                        continue;
                    }

                    String trimmed = line.trim();
                    String[] kv = trimmed.contains(":") ? trimmed.split(":", 2) : trimmed.split("=", 2);

                    if (kv.length != 2) continue;

                    String key = kv[0].trim();
                    String val = kv[1].trim().replaceAll("^\"|\"$", "");

                    if ("agent".equals(currentTop) && "exporter".equals(currentSecond)) {
                        switch (key) {
                            case "type":
                                cfg.exporterType = val;
                                break;
                            case "batchSize":
                                cfg.exporterBatchSize = Integer.parseInt(val);
                                break;
                            case "capacity":
                                cfg.exporterCapacity = Integer.parseInt(val);
                                break;
                            case "endpoint":
                                cfg.exporterEndpoint = val;
                                break;
                        }
                    } else if ("instrumentation".equals(currentTop)) {
                        switch (key) {
                            case "executor":
                                cfg.instrumentationExecutor = parseBool(val, cfg.instrumentationExecutor);
                                break;
                            case "completableFuture":
                                cfg.instrumentationCompletable = parseBool(val, cfg.instrumentationCompletable);
                                break;
                            case "httpClient":
                                cfg.instrumentationHttpClient = parseBool(val, cfg.instrumentationHttpClient);
                                break;
                            case "jdbc":
                                cfg.instrumentationJdbc = parseBool(val, cfg.instrumentationJdbc);
                                break;
                            case "scheduled":
                                cfg.instrumentationScheduled = parseBool(val, cfg.instrumentationScheduled);
                                break;
                            case "reactive":
                                cfg.instrumentationReactive = parseBool(val, cfg.instrumentationReactive);
                                break;
                        }
                    }
                }
            }
        } catch (Throwable t) {
            System.err.println("[YamlUtil] failed to load: " + t.getMessage());
        } finally {
            if (in != null) try {
                in.close();
            } catch (Exception ignored) {
            }
        }
        return cfg;
    }

    private static boolean parseBool(String v, boolean def) {
        if (v == null || v.isBlank()) return def;
        v = v.toLowerCase();
        return v.equals("1") || v.equals("true") || v.equals("yes");
    }
}

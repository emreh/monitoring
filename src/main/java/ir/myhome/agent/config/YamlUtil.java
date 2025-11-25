package ir.myhome.agent.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class YamlUtil {

    public static AgentConfig loadAgentConfig(String resourceName) {
        AgentConfig cfg = new AgentConfig();
        try (InputStream in = YamlUtil.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) return cfg; // defaults

            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String line;
                String currentTop = null;
                String currentSecond = null;

                while ((line = br.readLine()) != null) {
                    line = line.split("#", 2)[0];

                    if (line.trim().isEmpty()) continue;

                    if (!line.startsWith(" ") && line.endsWith(":")) {
                        currentTop = line.trim().replace(":", "");
                        currentSecond = null;
                        continue;
                    }

                    if (line.startsWith("  ") && line.trim().endsWith(":")) {
                        currentSecond = line.trim().replace(":", "");
                        continue;
                    }

                    // key: value or key = value
                    String trimmed = line.trim();
                    String[] kv = trimmed.contains(":") ? trimmed.split(":", 2) : trimmed.split("=", 2);

                    if (kv.length != 2) continue;

                    String key = kv[0].trim();
                    String val = kv[1].trim().replaceAll("^\"|\"$", "");

                    // map into cfg
                    if ("agent".equals(currentTop) && "exporter".equals(currentSecond)) {
                        switch (key) {
                            case "type" -> cfg.exporterType = val;
                            case "batchSize" -> cfg.exporterBatchSize = Integer.parseInt(val);
                            case "capacity" -> cfg.exporterCapacity = Integer.parseInt(val);
                            case "endpoint" -> cfg.exporterEndpoint = val;
                        }
                    } else if ("instrumentation".equals(currentTop)) {
                        switch (key) {
                            case "executor" ->
                                    cfg.instrumentationExecutor = parseBool(val, cfg.instrumentationExecutor);
                            case "completableFuture" ->
                                    cfg.instrumentationCompletable = parseBool(val, cfg.instrumentationCompletable);
                            case "httpClient" ->
                                    cfg.instrumentationHttpClient = parseBool(val, cfg.instrumentationHttpClient);
                            case "jdbc" -> cfg.instrumentationJdbc = parseBool(val, cfg.instrumentationJdbc);
                            case "scheduled" ->
                                    cfg.instrumentationScheduled = parseBool(val, cfg.instrumentationScheduled);
                            case "reactive" ->
                                    cfg.instrumentationReactive = parseBool(val, cfg.instrumentationReactive);
                            case "reactor" -> cfg.instrumentationReactor = parseBool(val, cfg.instrumentationReactor);
                            case "vertx" -> cfg.instrumentationVertx = parseBool(val, cfg.instrumentationVertx);
                            case "akka" -> cfg.instrumentationAkka = parseBool(val, cfg.instrumentationAkka);
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

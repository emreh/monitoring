package ir.myhome.agent.feature;

import ir.myhome.agent.config.AgentConfig;

public final class FeatureFlagManager {

    private final AgentConfig.InstrumentationConfig cfg;

    public FeatureFlagManager(AgentConfig.InstrumentationConfig cfg) {
        this.cfg = cfg;
    }

    public boolean timingEnabled() {
        return cfg.timing != null && cfg.timing.enabled;
    }

    public boolean executorEnabled() {
        return cfg.executor != null && cfg.executor.enabled;
    }

    public boolean jdbcEnabled() {
        return cfg.jdbc != null && cfg.jdbc.enabled;
    }

    public boolean httpClientEnabled() {
        return cfg.httpClient != null && cfg.httpClient.enabled;
    }

    public boolean scheduledEnabled() {
        return cfg.scheduled != null && cfg.scheduled.enabled;
    }

    public java.util.List<String> timingEntryPoints() {
        if (cfg.timing == null || cfg.timing.entrypoints == null) return java.util.Collections.emptyList();

        return cfg.timing.entrypoints;
    }
}

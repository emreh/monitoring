package ir.myhome.agent.feature;

import ir.myhome.agent.config.AgentConfig;

public final class FeatureFlagManager {

    private final AgentConfig.InstrumentationConfig cfg;

    public FeatureFlagManager(AgentConfig.InstrumentationConfig cfg) {
        this.cfg = cfg;
    }

    public boolean executor() {
        return cfg.executor;
    }

    public boolean jdbc() {
        return cfg.jdbc;
    }

    public boolean httpClient() {
        return cfg.httpClient;
    }

    public boolean timing() {
        return cfg.timing;
    }
}

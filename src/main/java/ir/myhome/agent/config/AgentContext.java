package ir.myhome.agent.config;

import ir.myhome.agent.collector.MetricCollector;

public final class AgentContext {
    private static volatile AgentConfig config;
    private static MetricCollector globalCollector;

    public static void init(AgentConfig cfg) {
        if (config != null) {
            return; // یا می‌توانی IllegalStateException بندازی
        }
        config = cfg;
    }

    public static AgentConfig getAgentConfig() {
        if (config == null) {
            throw new IllegalStateException("AgentContext not initialized");
        }
        return config;
    }

    public static void setCollector(MetricCollector col) {
        globalCollector = col;
    }

    public static MetricCollector getCollector() {
        return globalCollector;
    }
}

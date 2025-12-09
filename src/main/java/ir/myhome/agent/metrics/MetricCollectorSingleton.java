package ir.myhome.agent.metrics;

import ir.myhome.agent.config.AgentConfig;

public final class MetricCollectorSingleton {
    private static MetricCollector INSTANCE;

    public static void init(AgentConfig cfg) {
        if (INSTANCE == null)
            INSTANCE = new MetricCollector(cfg); // یا بدون cfg بسته به سازنده واقعی شما
    }

    public static MetricCollector get() {
        return INSTANCE;
    }
}

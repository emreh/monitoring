package ir.myhome.agent.metrics;

import ir.myhome.agent.config.AgentConfig;

public final class MetricCollectorSingleton {

    private static volatile MetricCollector instance;

    private MetricCollectorSingleton() {
    }

    public static void init(AgentConfig cfg) {
        if (instance == null) {
            synchronized (MetricCollectorSingleton.class) {
                if (instance == null) instance = new MetricCollector(cfg);
            }
        }
    }

    public static MetricCollector get() {
        return instance;
    }
}

package ir.myhome.agent.metrics;

import ir.myhome.agent.config.AgentConfig;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class MetricCollectorSingleton {

    private static final MetricCollectorSingleton INSTANCE = new MetricCollectorSingleton();
    private BlockingQueue<MetricSnapshot> exportQueue;
    private int queueCapacity = 10000;

    private MetricCollectorSingleton() {
        exportQueue = new LinkedBlockingQueue<>(queueCapacity);
    }

    public static MetricCollectorSingleton get() {
        return INSTANCE;
    }

    public void init(AgentConfig cfg) {
        if (cfg != null) {
            this.queueCapacity = cfg.queueCapacity;
            exportQueue = new LinkedBlockingQueue<>(queueCapacity);
        }
    }

    public BlockingQueue<MetricSnapshot> getExportQueue() {
        return exportQueue;
    }

    public void collect(MetricSnapshot snapshot) {
        exportQueue.offer(snapshot);
    }
}

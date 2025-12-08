package ir.myhome.agent.bootstrap;

import ir.myhome.agent.config.AgentConfig;
import ir.myhome.agent.config.AgentConfigLoader;
import ir.myhome.agent.config.AgentContext;
import ir.myhome.agent.exporter.ConsoleExporter;
import ir.myhome.agent.exporter.Exporter;
import ir.myhome.agent.exporter.HttpExporter;
import ir.myhome.agent.feature.FeatureFlagManager;
import ir.myhome.agent.holder.AgentHolder;
import ir.myhome.agent.metrics.AgentMetrics;
import ir.myhome.agent.queue.BoundedSpanQueue;
import ir.myhome.agent.queue.SpanQueue;
import ir.myhome.agent.ui.StatusServer;
import ir.myhome.agent.worker.BatchWorker;

import java.lang.instrument.Instrumentation;

public final class AgentMain {

    private AgentMain() {
    }

    /**
     * Java Agent entrypoint
     */
    public static void premain(String args, Instrumentation inst) {
        System.out.println("[AgentMain] starting agent...");

        // ------------------------------------------------------------
        // 1) Load config
        // ------------------------------------------------------------
        AgentConfig cfg = AgentConfigLoader.loadYaml("agent-config.yml", AgentConfig.class);
        if (cfg == null) {
            throw new IllegalStateException("Agent config not loaded");
        }

        AgentContext.init(cfg);
        System.out.println("[AgentMain] config loaded: " + cfg);

        // ------------------------------------------------------------
        // 2) Feature flags
        // ------------------------------------------------------------
        FeatureFlagManager featureFlags = new FeatureFlagManager(cfg.instrumentation);
        AgentHolder.setFeatureFlagManager(featureFlags);

        // ------------------------------------------------------------
        // 3) Queue
        // ------------------------------------------------------------
        SpanQueue queue = new BoundedSpanQueue(cfg.exporter.capacity);
        AgentHolder.setSpanQueue(queue);

        // ------------------------------------------------------------
        // 4) Exporter
        // ------------------------------------------------------------
        Exporter exporter;
        if ("http".equalsIgnoreCase(cfg.exporter.type)) {
            exporter = new HttpExporter(cfg.exporter.endpoint);
            System.out.println("[AgentMain] HttpExporter enabled -> " + cfg.exporter.endpoint);
        } else {
            exporter = new ConsoleExporter();
            System.out.println("[AgentMain] ConsoleExporter enabled");
        }
        AgentHolder.setExporter(exporter);

        // ------------------------------------------------------------
        // 5) Metrics
        // ------------------------------------------------------------
        AgentMetrics metrics = new AgentMetrics();

        // ------------------------------------------------------------
        // 6) Background worker
        // ------------------------------------------------------------
        Thread workerThread = new Thread(new BatchWorker(queue, exporter, cfg.exporter.batchSize, 20, metrics), "agent-batch-worker");
        workerThread.setDaemon(true);
        workerThread.start();

        // ------------------------------------------------------------
        // 7) Status HTTP server (debug / observability)
        // ------------------------------------------------------------
        try {
            StatusServer statusServer = new StatusServer(8081, metrics);
            statusServer.start();
        } catch (Exception e) {
            System.err.println("[AgentMain] status server failed: " + e.getMessage());
        }

        // ------------------------------------------------------------
        // 8) Install instrumentation
        // ------------------------------------------------------------
        try {
            InstrumentationInstaller.install(inst, cfg);
        } catch (Throwable t) {
            System.err.println("[AgentMain] instrumentation install failed: " + t.getMessage());
            t.printStackTrace();
        }

        System.out.println("[AgentMain] agent started successfully");
    }
}

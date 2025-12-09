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
import java.util.ArrayList;
import java.util.List;

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
        SpanQueue queue = new BoundedSpanQueue(cfg.queueCapacity);
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
        // 6) Background worker(s)
        // ------------------------------------------------------------
        List<Thread> workers = new ArrayList<>();
        int workerCount = Math.max(1, cfg.workerCount);
        for (int i = 0; i < workerCount; i++) {
            Thread workerThread = new Thread(new BatchWorker(queue, exporter, cfg.exporter.batchSize, cfg.pollMillis, metrics), "agent-batch-worker-" + i);
            workerThread.setDaemon(true);
            workerThread.start();
            workers.add(workerThread);
        }

        // ------------------------------------------------------------
        // 7) Status HTTP server (debug / observability)
        // ------------------------------------------------------------
        StatusServer statusServer = null;
        try {
            statusServer = new StatusServer(8082, metrics);
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

        // ------------------------------------------------------------
        // 9) Shutdown hook -> graceful stop
        // ------------------------------------------------------------
        final StatusServer finalStatusServer = statusServer;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[AgentMain] shutdown initiated");
            try {
                // interrupt workers
                for (Thread wt : workers) {
                    wt.interrupt();
                }
                // give them a bit to finish
                for (Thread wt : workers) {
                    try {
                        wt.join(2000);
                    } catch (InterruptedException ignored) {
                    }
                }
                // stop status server
                if (finalStatusServer != null) finalStatusServer.stop();

                // close exporter if needed
                try {
                    AgentHolder.getExporter().close();
                } catch (Throwable ignore) {
                }

                System.out.println("[AgentMain] shutdown complete");
            } catch (Throwable t) {
                System.err.println("[AgentMain] shutdown error: " + t.getMessage());
            }
        }, "agent-shutdown-hook"));

        System.out.println("[AgentMain] agent started successfully");
    }
}

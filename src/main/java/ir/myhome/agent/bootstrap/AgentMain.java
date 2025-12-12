package ir.myhome.agent.bootstrap;

import ir.myhome.agent.collector.MetricCollector;
import ir.myhome.agent.config.AgentConfig;
import ir.myhome.agent.config.AgentConfigLoader;
import ir.myhome.agent.config.AgentContext;
import ir.myhome.agent.exporter.BatchExporter;
import ir.myhome.agent.exporter.ConsoleExporter;
import ir.myhome.agent.exporter.HttpExporter;
import ir.myhome.agent.feature.FeatureFlagManager;
import ir.myhome.agent.scheduler.PercentileBatchScheduler;
import ir.myhome.agent.ui.StatusServer;
import ir.myhome.agent.worker.MetricExporterWorker;

import java.lang.instrument.Instrumentation;

public final class AgentMain {

    private AgentMain() {
    }

    public static void premain(String args, Instrumentation inst) {
        System.out.println("[AgentMain] starting agent...");

        // ---------------- Load Config ----------------
        AgentConfig cfg = AgentConfigLoader.loadYaml("agent-config.yml", AgentConfig.class);
        if (cfg == null) throw new IllegalStateException("Agent config not loaded");
        AgentContext.init(cfg);

        // ---------------- Feature flags ----------------
        FeatureFlagManager ffm = new FeatureFlagManager(cfg.instrumentation);
        // اگر لازم است ذخیره کن: AgentContext.setFeatureFlagManager(ffm);

        // ---------------- Metric Collector ----------------
        MetricCollector collector = new MetricCollector(cfg);

        // ---------------- Exporter انتخابی ----------------
        BatchExporter batchExporter = null;
        Runnable exporterRunnable = null;

        String expType = cfg.exporter != null && cfg.exporter.type != null ? cfg.exporter.type.toLowerCase() : "console";
        switch (expType) {
            case "http" -> {
                // فرض بر این است که HttpExporter implements Runnable
                exporterRunnable = new HttpExporter(cfg.exporter.endpoint);
            }
            case "batch" -> {
                exporterRunnable = new BatchExporter(collector.getExportQueue(), cfg.exporter.batchSize, 2000);
            }
            default -> {
                // فرض بر این است که ConsoleExporter implements Runnable
                exporterRunnable = new ConsoleExporter();
            }
        }

        Thread exporterThread = null;
        if (exporterRunnable != null) {
            exporterThread = new Thread(exporterRunnable, "ExporterThread");
            exporterThread.setDaemon(true);
            exporterThread.start();
            System.out.println("[AgentMain] exporter thread started: " + exporterRunnable.getClass().getSimpleName());
        } else {
            System.err.println("[AgentMain] WARNING: exporterRunnable is null — no exporter started");
        }

        // ---------------- Status Server ----------------
        StatusServer statusServer = null;
        try {
            statusServer = new StatusServer(8082, collector.getExportQueue());
            statusServer.start();
            System.out.println("[AgentMain] status server started at http://localhost:8082/status");
        } catch (Exception e) {
            System.err.println("[AgentMain] status server failed: " + e.getMessage());
        }

        // ---------------- MetricExporterWorker ----------------
        MetricExporterWorker exporterWorker = new MetricExporterWorker(collector.getExportQueue(), cfg.exporter.batchSize, cfg.pollMillis);
        exporterWorker.setDaemon(true);
        exporterWorker.start();
        System.out.println("[AgentMain] MetricExporterWorker started");

        // ---------------- Instrumentation ----------------
        try {
            InstrumentationInstaller.install(inst, cfg);
        } catch (Throwable t) {
            System.err.println("[AgentMain] instrumentation install failed: " + t.getMessage());
            t.printStackTrace();
        }

        // 6. Start percentile scheduler
        PercentileBatchScheduler.start();
        System.out.println("[AgentMain] PercentileBatchScheduler started successfully");

        // ---------------- Shutdown Hook ----------------
        final StatusServer finalStatusServer = statusServer;
        final BatchExporter finalBatchExporter = batchExporter;
        final Thread finalExporterThread = exporterThread;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[AgentMain] shutdown initiated");
            if (finalStatusServer != null) finalStatusServer.stop();

            if (finalBatchExporter != null) {
                try {
                    finalBatchExporter.shutdown();
                } catch (Throwable t) {
                    System.err.println("[AgentMain] batchExporter.shutdown failed: " + t.getMessage());
                }
            }

            if (finalExporterThread != null) {
                finalExporterThread.interrupt();
                try {
                    finalExporterThread.join(3000);
                } catch (InterruptedException ignored) {
                }
            }

            try {
                exporterWorker.shutdown();
            } catch (Throwable t) {
                System.err.println("[AgentMain] exporterWorker.shutdown failed: " + t.getMessage());
            }

            // stop percentile scheduler
            PercentileBatchScheduler.stop();
            System.out.println("[AgentMain] percentile scheduler stopped");

        }, "agent-shutdown-hook"));

        System.out.println("[AgentMain] agent started successfully");
    }
}

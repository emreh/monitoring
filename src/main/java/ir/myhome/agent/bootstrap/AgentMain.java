package ir.myhome.agent.bootstrap;

import ir.myhome.agent.collector.*;
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

        new FeatureFlagManager(cfg.instrumentation);

        // ---------------- Metric Collector ----------------
        MetricCollector collector = new MetricCollector(cfg);

        // ---------------- Exporter انتخابی ----------------
        Runnable exporterRunnable = switch (cfg.exporter.type.toLowerCase()) {
            case "http" -> new HttpExporter(cfg.exporter.endpoint);
            case "batch" -> new BatchExporter(collector.getExportQueue(), cfg.exporter.batchSize, 2000);
            default -> new ConsoleExporter();
        };
        Thread exporterThread = new Thread(exporterRunnable, "ExporterThread");
        exporterThread.setDaemon(true);
        exporterThread.start();

        // ---------------- Status Server ----------------
        StatusServer statusServer = null;
        try {
            statusServer = new StatusServer(8082, collector.getExportQueue());
            statusServer.start();
        } catch (Exception e) {
            System.err.println("[AgentMain] status server failed: " + e.getMessage());
        }

        // ---------------- MetricExporterWorker ----------------
        MetricExporterWorker exporterWorker = new MetricExporterWorker(collector.getExportQueue(), cfg.exporter.batchSize, cfg.pollMillis);
        exporterWorker.setDaemon(true);
        exporterWorker.start();

        // ---------------- Instrumentation ----------------
        try {
            InstrumentationInstaller.install(inst, cfg);
        } catch (Throwable t) {
            System.err.println("[AgentMain] instrumentation install failed: " + t.getMessage());
            t.printStackTrace();
        }

        // ---------------- Composite Percentile Collector ----------------
        //1 میلی‌ثانیه کمترین مقدار قابل اندازه‌گیری در HDRHistogram
        //3600000 بالاترین مقدار (مثلا یک ساعت به میلی‌ثانیه)
        //3 دقت اعشاری
        //100.0 ضریب فشرده‌سازی t-digest
        PercentileCollector percentileCollector = new PercentileOrchestrator(new HDRHistogramCollector(1, 3600000, 3), new TDigestCollector(100.0));
        PercentileBatchScheduler.start(percentileCollector, 2000);

        // ---------------- Shutdown Hook ----------------
        final StatusServer finalStatusServer = statusServer;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[AgentMain] shutdown initiated");
            if (finalStatusServer != null) finalStatusServer.stop();
            exporterThread.interrupt();
            exporterWorker.shutdown();
            PercentileBatchScheduler.stop();
        }, "agent-shutdown-hook"));

        System.out.println("[AgentMain] agent started successfully");
    }
}

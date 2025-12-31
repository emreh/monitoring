package ir.myhome.agent.bootstrap;

import ir.myhome.agent.collector.*;
import ir.myhome.agent.config.AgentConfig;
import ir.myhome.agent.config.AgentConfigLoader;
import ir.myhome.agent.config.AgentContext;
import ir.myhome.agent.exporter.BatchExporter;
import ir.myhome.agent.feature.FeatureFlagManager;
import ir.myhome.agent.scheduler.PercentileBatchScheduler;
import ir.myhome.agent.status.MonitoringServer;
import ir.myhome.agent.status.StatusServer;
import ir.myhome.agent.worker.MetricExporterWorker;

import java.lang.instrument.Instrumentation;

public class AgentMain {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("[AgentMain] Starting custom agent for Phase 13...");

        try {
            // 1. بارگذاری تنظیمات
            AgentConfig cfg = AgentConfigLoader.loadYaml("agent-config.yml", AgentConfig.class);
            if (cfg == null) throw new IllegalStateException("Agent config not loaded");
            AgentContext.init(cfg);
            new FeatureFlagManager(cfg.instrumentation);

            // 2. مقداردهی کالکتور اصلی
            MetricCollector collector = new MetricCollector(cfg);
            AgentContext.setCollector(collector);

            try {
                // پاس دادن کالکتور به سرور برای دسترسی به صف و متریک‌ها
                MonitoringServer monitoringUI = new MonitoringServer(8083, collector);
                monitoringUI.start();
                System.out.println("[AgentMain] Monitoring UI started on port 8083");
            } catch (Exception e) {
                System.err.println("[AgentMain] UI failed: " + e.getMessage());
            }

            // 4. مدیریت Exporterها بر اساس فایل تنظیمات
            String type = cfg.exporter.type.toLowerCase();
            if ("batch".equals(type)) {
                // BatchExporter خودش ترد داخلی دارد و نیاز به استارت دستی به عنوان Runnable ندارد
                new BatchExporter(collector.getExportQueue(), cfg.exporter.batchSize, 2000);
            } else {
                // برای سایر حالت‌ها مثل HTTP یا Console، از Worker استفاده می‌کنیم
                // توجه: اگر HttpExporter دارید، منطق ارسال در MetricExporterWorker است
                MetricExporterWorker exporterWorker = new MetricExporterWorker(collector.getExportQueue(), cfg.exporter.batchSize, cfg.pollMillis);
                exporterWorker.setDaemon(true);
                exporterWorker.start();
            }

            // 5. راه‌اندازی Status Server (برای APIهای متنی)
            try {
                StatusServer statusServer = new StatusServer(8082, collector.getExportQueue());
                statusServer.start();
            } catch (Exception e) {
                System.err.println("[AgentMain] StatusServer error: " + e.getMessage());
            }

            // 6. نصب اینسترومنتاسیون (تزریق به کدها)
            InstrumentationInstaller.install(inst, cfg);

            // 7. راه‌اندازی تحلیل‌های آماری (Percentiles)
            PercentileCollector percentileCollector = new PercentileOrchestrator(new HDRHistogramCollector(1, 3600000, 3), new TDigestCollector(100.0));
            PercentileBatchScheduler.start(percentileCollector, 2000);

            // 8. Shutdown Hook برای بستن تمیز منابع
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("[AgentMain] Shutdown initiated...");
                PercentileBatchScheduler.stop();
            }));

            System.out.println("[AgentMain] Agent started successfully.");

        } catch (Throwable t) {
            System.err.println("[AgentMain] FATAL ERROR during agent startup:");
            t.printStackTrace();
        }
    }
}
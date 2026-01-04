package ir.myhome.agent.bootstrap;

import ir.myhome.agent.collector.SpanCollector;
import ir.myhome.agent.config.AgentConfig;
import ir.myhome.agent.core.Aggregator;
import ir.myhome.agent.core.MetricsAggregator;
import ir.myhome.agent.core.Span;
import ir.myhome.agent.exporter.AgentExporter;
import ir.myhome.agent.exporter.impl.BatchExporter;
import ir.myhome.agent.exporter.impl.ConsoleExporter;
import ir.myhome.agent.exporter.impl.FileExporter;
import ir.myhome.agent.exporter.impl.KafkaExporter;
import ir.myhome.agent.policy.PolicyStats;
import ir.myhome.agent.policy.ReferencePolicy;
import ir.myhome.agent.policy.SafePolicyEngine;
import ir.myhome.agent.policy.contract.PolicyEngine;
import ir.myhome.agent.queue.SpanQueue;
import ir.myhome.agent.queue.SpanQueueImpl;
import ir.myhome.agent.status.StatusServer;
import ir.myhome.agent.util.SystemLoadCalculator;
import ir.myhome.agent.worker.BatchWorker;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;

public class AgentMain {

    public static void premain(String agentArgs, Instrumentation inst) {
        try {
            // ۱. بارگذاری تنظیمات
            AgentConfig cfg = new AgentConfig();
            if (cfg == null) {
                throw new IllegalStateException("Failed to load AgentConfig.");
            }

            // ایجاد شیء Aggregator
            MetricsAggregator metricsAggregator = new MetricsAggregator();
            Aggregator aggregator = new Aggregator(metricsAggregator);

            // ایجاد شیء PolicyStats و SafePolicyEngine
            PolicyStats stats = new PolicyStats();
            PolicyEngine policyEngineDelegate = new ReferencePolicy(SystemLoadCalculator.calculateSampleRate());  // فرضاً این یک موتور سیاست است
            SafePolicyEngine policyEngine = new SafePolicyEngine(policyEngineDelegate, stats);

            // ۲. راه‌اندازی صف برای نگهداری داده‌ها
            SpanQueue<Span> spanQueue = new SpanQueueImpl<>(10000);  // استفاده از پیاده‌سازی واقعی SpanQueue
            SpanCollector collector = new SpanCollector(spanQueue, aggregator);  // هماهنگ با کلاس SpanCollector

            System.out.println("collector => " + collector);

            // ۳. راه‌اندازی BatchExporter برای ارسال داده‌ها
            int batchSize = cfg.exporter != null ? cfg.exporter.batchSize : 100;
            if (batchSize <= 0) {
                throw new IllegalArgumentException("Batch size must be greater than zero.");
            }

            List<AgentExporter> exporters = new ArrayList<>();
            exporters.add(new KafkaExporter("localhost:9092", "agent-spans", true, aggregator));
            exporters.add(new ConsoleExporter(true, aggregator));
            exporters.add(new FileExporter(true, aggregator));

            BatchExporter batchExporter = new BatchExporter(spanQueue, batchSize, 2000, exporters, aggregator, policyEngine);  // هماهنگ با BatchExporter

            // ۴. راه‌اندازی BatchWorker برای پردازش داده‌ها
            BatchWorker worker = new BatchWorker(spanQueue, batchExporter, batchSize, 2000);  // هماهنگ با BatchWorker
            Thread workerThread = new Thread(worker, "Agent-BatchWorker-Thread");
            workerThread.setDaemon(true);  // تنظیم کارگر به صورت daemon برای جلوگیری از بلوکه شدن برنامه اصلی
            workerThread.start();

            // بررسی وضعیت thread و اینکه آیا کارکرد درستی داره یا نه
            if (!workerThread.isAlive()) {
                throw new IllegalStateException("Worker thread failed to start.");
            }

            // ۵. راه‌اندازی StatusServer برای مشاهده وضعیت سیستم
            int port = cfg.statusPort > 0 ? cfg.statusPort : 8082; // استفاده از پورت تنظیمات یا پیش‌فرض
            StatusServer statusServer = new StatusServer(port, spanQueue);  // هماهنگ با StatusServer
            statusServer.start();
            System.out.println("[AgentMain] StatusServer started on port " + port);

            // 6. نصب اینسترومنتاسیون (تزریق به کدها)
            InstrumentationInstaller.install(inst, cfg);

            // ۶. چاپ موفقیت
            System.out.println("[AgentMain] Agent initialized successfully with BatchWorker.");

        } catch (IllegalStateException e) {
            // اینجا خطاهای غیرقابل جبران رو می‌گیریم و واضح‌تر گزارش می‌کنیم
            System.err.println("[AgentMain] Fatal Configuration Error: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // بررسی برای خطاهای آرگومان‌ها
            System.err.println("[AgentMain] Invalid argument: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // خطاهای غیرمنتظره
            System.err.println("[AgentMain] Unknown Fatal Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

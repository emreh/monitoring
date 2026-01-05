package ir.myhome.agent.bootstrap;

import ir.myhome.agent.collector.*;
import ir.myhome.agent.config.AgentConfig;
import ir.myhome.agent.core.Aggregator;
import ir.myhome.agent.core.MetricsAggregator;
import ir.myhome.agent.core.Span;
import ir.myhome.agent.exporter.AgentExporter;
import ir.myhome.agent.exporter.impl.*;
import ir.myhome.agent.policy.PolicyStats;
import ir.myhome.agent.policy.ReferencePolicy;
import ir.myhome.agent.policy.SafePolicyEngine;
import ir.myhome.agent.policy.contract.PolicyEngine;
import ir.myhome.agent.queue.SpanQueue;
import ir.myhome.agent.queue.SpanQueueImpl;
import ir.myhome.agent.scheduler.PercentileBatchScheduler;
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

            // پیکربندی یا راه‌اندازی سیستم
            PercentileCollector percentileCollector = new PercentileOrchestrator(new HDRHistogramCollector(1, 1000000, 2), new TDigestCollector(100.0));
            long flushIntervalMs = 5000; // فاصله زمانی برای گزارش درصدها (مثلاً هر 5 ثانیه)

            // فراخوانی start برای راه‌اندازی زمان‌بندی پردازش پرسنترایل‌ها
            PercentileBatchScheduler.start(percentileCollector, flushIntervalMs);

            //// ایجاد صف برای ذخیره داده‌ها
            // تنظیم ظرفیت صف
            SpanQueue<Long> queue = new SpanQueueImpl<>(10000);
            // تعداد داده‌هایی که در هر بار صادر می‌شوند
            int batchSize = cfg.exporter != null ? cfg.exporter.batchSize : 100;

            // ایجاد نمونه از InstrumentedAsyncExporter
            // LatencyCollector = وظیفه ثبت تاخیرها
            InstrumentedAsyncExporter<Long> exporter = new InstrumentedAsyncExporter<>(queue, batchSize, new LatencyCollector(1000, 2));

            // شروع فرآیند صادر کردن داده‌ها
            exporter.start();

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

            // ۳. راه‌اندازی BatchExporter برای ارسال داده‌ها
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

            // نصب اینسترومنتاسیون (تزریق به کدها)
            InstrumentationInstaller.install(inst, cfg);

            // نصب اینسترومنتاسیون برای زمان ورود و خروج
            OptimizedInstrumentation.installInstrumentation("com.example.services");

            // ۶. چاپ موفقیت
            System.out.println("[AgentMain] Agent initialized successfully with BatchWorker.");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("[AgentMain] shutdown initiated...");
                PercentileBatchScheduler.stop();
                exporter.stop();
                System.out.println("[AgentMain] stopped successfully.");
            }, "agent-shutdown-hook"));

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

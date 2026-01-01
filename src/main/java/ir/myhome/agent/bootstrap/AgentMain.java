package ir.myhome.agent.bootstrap;

import ir.myhome.agent.config.AgentConfig;
import ir.myhome.agent.collector.SpanCollector; // اصلاح مسیر کلکتور
import ir.myhome.agent.exporter.impl.BatchExporter; // اصلاح مسیر اکسپورتر
import ir.myhome.agent.status.StatusServer; // اصلاح مسیر سرور وضعیت
import ir.myhome.agent.worker.BatchWorker; // اضافه کردن ورکر واقعی
import java.lang.instrument.Instrumentation;

public class AgentMain {

    public static void premain(String agentArgs, Instrumentation inst) {
        try {
            // ۱. بارگذاری تنظیمات
            AgentConfig cfg = new AgentConfig();

            // ۲. راه‌اندازی کلکتور واقعی (SpanCollector)
            // طبق درخت شما، احتمالا این کلکتور باید به صف وصل شود
            SpanCollector collector = new SpanCollector();

            // ۳. راه‌اندازی اکسپورتر از پکیج impl
            int batchSize = cfg.exporter != null ? cfg.exporter.batchSize : 100;
            BatchExporter batchExporter = new BatchExporter(collector.getQueue(), batchSize, 2000);

            // ۴. **اصلاح باگ اصلی**: راه‌اندازی BatchWorker برای اینکه عملیات ارسال انجام شود
            BatchWorker worker = new BatchWorker(batchExporter, collector);
            Thread workerThread = new Thread(worker, "Agent-BatchWorker-Thread");
            workerThread.setDaemon(true);
            workerThread.start();

            // ۵. راه‌اندازی سرور وضعیت از پکیج status
            try {
                // پورت را از کانفیگ بگیر، اگر نبود ۸۰۸۲
                int port = 8082;
                StatusServer statusServer = new StatusServer(port, collector.getQueue());
                statusServer.start();
                System.out.println("[AgentMain] StatusServer started on port " + port);
            } catch (Exception e) {
                System.err.println("[AgentMain] StatusServer failed: " + e.getMessage());
            }

            // ۶. ثبت ترانسفورمر (احتمالا نامش در پروژه شما InstrumentationInstaller یا مشابه است)
            // inst.addTransformer(new AgentTransformer(collector));

            System.out.println("[AgentMain] Agent initialized successfully with BatchWorker.");

        } catch (Exception e) {
            System.err.println("[AgentMain] Fatal Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
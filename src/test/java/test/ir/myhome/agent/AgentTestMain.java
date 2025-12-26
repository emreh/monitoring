package test.ir.myhome.agent;

import ir.myhome.agent.config.AgentConfig;
import ir.myhome.agent.core.Span;
import ir.myhome.agent.exporter.AgentExporter;
import ir.myhome.agent.exporter.ConsoleExporter;
import ir.myhome.agent.exporter.HttpExporter;
import ir.myhome.agent.holder.AgentHolder;
import ir.myhome.agent.queue.SpanQueueImpl;
import ir.myhome.agent.status.StatusServer;
import ir.myhome.agent.worker.BatchWorker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AgentTestMain {

    public static void main(String[] args) throws Exception {
        AgentConfig config = new AgentConfig();

        // Queue
        SpanQueueImpl<Span> queue = new SpanQueueImpl<>(config.queueCapacity);

        // Exporter
        AgentExporter exporter;
        if ("console".equalsIgnoreCase(config.exporter.type)) {
            exporter = new ConsoleExporter();
        } else {
            exporter = new HttpExporter(config.exporter.endpoint);
        }

        // BatchWorker
        BatchWorker worker = new BatchWorker(queue, exporter, config);
        Thread workerThread = new Thread(worker);
        workerThread.start();

        AgentHolder.setExporter(exporter);
        AgentHolder.setSpanQueue(queue);

        // StatusServer
        BlockingQueue<ir.myhome.agent.metrics.MetricSnapshot> statusQueue =
                new LinkedBlockingQueue<>();
        StatusServer statusServer = new StatusServer(config.statusPort, statusQueue);
        statusServer.start();

        // تولید چند Span نمونه
        for (int i = 0; i < 20; i++) {
            long now = System.currentTimeMillis();
            Span span = new Span(
                    "trace-" + i,     // traceId
                    "span-" + i,      // spanId
                    null,             // parentId
                    "test-service",   // service
                    "/test-endpoint", // endpoint
                    now               // startEpochMs
            );
            span.end();
            queue.offer(span);
            Thread.sleep(50); // شبیه‌سازی فاصله زمانی بین span ها
        }

        System.out.println("[AgentTestMain] Test spans queued.");

        // اجازه بدید worker چند ثانیه پردازش کند
        Thread.sleep(2000);

        worker.stop();
        workerThread.join();

        statusServer.stop();
        System.out.println("[AgentTestMain] Test completed.");
    }
}

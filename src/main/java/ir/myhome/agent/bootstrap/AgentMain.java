package ir.myhome.agent.bootstrap;

import ir.myhome.agent.config.AgentConfig;
import ir.myhome.agent.core.Span;
import ir.myhome.agent.exporter.AgentExporter;
import ir.myhome.agent.exporter.ConsoleExporter;
import ir.myhome.agent.exporter.HttpExporter;
import ir.myhome.agent.holder.AgentHolder;
import ir.myhome.agent.queue.SpanQueue;
import ir.myhome.agent.queue.SpanQueueImpl;
import ir.myhome.agent.worker.BatchWorker;
import ir.myhome.agent.status.StatusServer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AgentMain {

    public static void main(String[] args) throws Exception {
        AgentConfig config = new AgentConfig();

        // Queue
        SpanQueue<Span> queue = new SpanQueueImpl<>(config.queueCapacity);

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
        for (int i = 0; i < 5; i++) {
            long now = System.currentTimeMillis();
            Span span = new Span("trace-" + i, "span-" + i, null,
                    "demo-service", "/demo-endpoint", now);
            span.end();
            queue.offer(span);
        }

        Thread.sleep(2000); // اجازه پردازش

        worker.stop();
        workerThread.join();
        statusServer.stop();
    }
}

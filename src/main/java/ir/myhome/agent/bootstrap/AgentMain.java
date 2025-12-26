package ir.myhome.agent.bootstrap;

import ir.myhome.agent.config.AgentConfig;
import ir.myhome.agent.exporter.AgentExporter;
import ir.myhome.agent.exporter.ConsoleExporter;
import ir.myhome.agent.exporter.HttpExporter;
import ir.myhome.agent.holder.AgentHolder;
import ir.myhome.agent.metrics.MetricSnapshot;
import ir.myhome.agent.queue.SpanQueueImpl;
import ir.myhome.agent.status.StatusServer;
import ir.myhome.agent.worker.BatchWorker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AgentMain {

    public static void main(String[] args) throws Exception {
        AgentConfig config = new AgentConfig();

        // Queue و BatchWorker
        SpanQueueImpl<ir.myhome.agent.core.Span> queue = new SpanQueueImpl<>(config.queueCapacity);

        AgentExporter exporter;
        if ("console".equalsIgnoreCase(config.exporter.type)) {
            exporter = new ConsoleExporter();
        } else {
            exporter = new HttpExporter(config.exporter.endpoint);
        }

        BatchWorker worker = new BatchWorker(queue, exporter, config);
        Thread workerThread = new Thread(worker);
        workerThread.start();

        AgentHolder.setExporter(exporter);
        AgentHolder.setSpanQueue(queue);

        // StatusServer
        BlockingQueue<MetricSnapshot> statusQueue = new LinkedBlockingQueue<>();
        StatusServer statusServer = new StatusServer(config.statusPort, statusQueue);
        statusServer.start();

        System.out.println("[AgentMain] Agent started successfully.");
    }
}

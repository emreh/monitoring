package test.ir.myhome.agent;

import ir.myhome.agent.config.AgentConfig;
import ir.myhome.agent.config.AgentConfigLoader;
import ir.myhome.agent.config.AgentContext;
import ir.myhome.agent.exporter.ConsoleExporter;
import ir.myhome.agent.exporter.Exporter;
import ir.myhome.agent.holder.AgentHolder;
import ir.myhome.agent.metrics.AgentMetrics;
import ir.myhome.agent.queue.BoundedSpanQueue;
import ir.myhome.agent.ui.StatusServer;
import ir.myhome.agent.worker.BatchWorker;

import java.util.HashMap;
import java.util.Map;

public class TestBootstrap {

    public static void main(String[] args) throws Exception {
        AgentConfig cfg = AgentConfigLoader.loadYaml("agent-config.yml", AgentConfig.class);

        if (cfg == null) throw new IllegalStateException("config null");

        AgentContext.init(cfg);

        var queue = new BoundedSpanQueue(cfg.exporter.capacity);
        AgentHolder.setSpanQueue(queue);

        Exporter exporter = "http".equalsIgnoreCase(cfg.exporter.type) ? new ir.myhome.agent.exporter.HttpExporter(cfg.exporter.endpoint) : new ConsoleExporter();
        AgentHolder.setExporter(exporter);

        AgentMetrics metrics = new AgentMetrics();
        Thread worker = new Thread(new BatchWorker(queue, exporter, cfg.exporter.batchSize, 20, metrics), "BatchWorkerThread");
        worker.setDaemon(true);
        worker.start();

        StatusServer ss = new StatusServer(8081, metrics);
        ss.start();

        // simulate producing spans
        for (int i = 0; i < 25; i++) {
            Map<String, Object> sample = new HashMap<>();
            sample.put("id", i);
            sample.put("msg", "hello " + i);
            queue.offer(sample);
            Thread.sleep(150);
        }

        Thread.sleep(5000);
        System.out.println("done test");
        ss.stop();
        // exporter.close() if needed
    }
}

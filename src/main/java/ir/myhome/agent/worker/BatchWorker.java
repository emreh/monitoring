package ir.myhome.agent.worker;

import ir.myhome.agent.config.AgentConfig;
import ir.myhome.agent.core.Span;
import ir.myhome.agent.exporter.AgentExporter;
import ir.myhome.agent.queue.SpanQueue;

import java.util.ArrayList;
import java.util.List;

public final class BatchWorker implements Runnable {

    private final SpanQueue<Span> queue;
    private final AgentExporter exporter;
    private final int batchSize;
    private volatile boolean running = true;

    public BatchWorker(SpanQueue<Span> queue, AgentExporter exporter, AgentConfig cfg) {
        this.queue = queue;
        this.exporter = exporter;
        this.batchSize = cfg.exporter.batchSize;
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        running = false;
    }

    public void drainAndExport() {
        List<Span> batch = new ArrayList<>();
        Span span;
        while ((span = queue.poll()) != null && batch.size() < batchSize) {
            batch.add(span);
        }
        if (!batch.isEmpty()) {
            exporter.export(batch);
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                drainAndExport();
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}

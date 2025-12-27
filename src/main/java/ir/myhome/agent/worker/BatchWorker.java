package ir.myhome.agent.worker;

import ir.myhome.agent.config.AgentConfig;
import ir.myhome.agent.core.Span;
import ir.myhome.agent.exporter.AgentExporter;
import ir.myhome.agent.queue.SpanQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class BatchWorker implements Runnable {

    private final SpanQueue<Span> queue;
    private final AgentExporter exporter;
    private final int batchSize;
    private final long exportIntervalMillis;

    private final AtomicBoolean running = new AtomicBoolean(true);
    private volatile Thread workerThread;

    public BatchWorker(SpanQueue<Span> queue, AgentExporter exporter, AgentConfig config) {
        this.queue = queue;
        this.exporter = exporter;
        this.batchSize = config.exporter.batchSize;
        this.exportIntervalMillis = config.pollMillis;
    }

    public void stop() {
        running.set(false);
        if (workerThread != null) {
            workerThread.interrupt(); // Interrupt sleep for fast shutdown
        }
    }

    public boolean isRunning() {
        return running.get();
    }

    @Override
    public void run() {
        workerThread = Thread.currentThread();
        List<Span> batch = new ArrayList<>(batchSize);

        try {
            while (running.get() && !Thread.currentThread().isInterrupted()) {
                batch.clear();
                int drained = queue.drainTo(batch, batchSize);

                if (drained > 0) {
                    safeExport(batch);
                }

                try {
                    Thread.sleep(exportIntervalMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } finally {
            flushRemaining(batch);
        }
    }

    private void safeExport(List<Span> batch) {
        try {
            exporter.export(batch);
        } catch (Throwable t) {
            // Stage 12: isolate failure, no retry
            System.err.println("Exporter failed: " + t.getMessage());
        }

        // Log dropped spans
        long dropped = queue.dropped();
        if (dropped > 0) {
            System.err.println("Stage 12 Warning: " + dropped + " spans were dropped.");
        }
    }

    private void flushRemaining(List<Span> batch) {
        batch.clear();
        int drained;
        do {
            drained = queue.drainTo(batch, batchSize);
            if (!batch.isEmpty()) {
                safeExport(batch);
                batch.clear();
            }
        } while (drained > 0);
    }
}

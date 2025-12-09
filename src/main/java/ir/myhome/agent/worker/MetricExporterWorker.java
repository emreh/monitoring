package ir.myhome.agent.worker;

import ir.myhome.agent.metrics.MetricSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public final class MetricExporterWorker extends Thread {

    private final BlockingQueue<MetricSnapshot> queue;
    private volatile boolean running = true;
    private final long pollTimeoutMs;
    private final int batchSize;

    public MetricExporterWorker(BlockingQueue<MetricSnapshot> queue, long pollTimeoutMs, int batchSize) {
        this.queue = queue;
        this.pollTimeoutMs = pollTimeoutMs;
        this.batchSize = batchSize;
        setName("MetricExporterWorker");
        setDaemon(true); // Thread پس‌زمینه
    }

    @Override
    public void run() {
        try {
            while (running) {
                try {
                    List<MetricSnapshot> batch = new ArrayList<>(batchSize);
                    MetricSnapshot first = queue.poll(pollTimeoutMs, TimeUnit.MILLISECONDS);
                    if (first != null) {
                        batch.add(first);
                        queue.drainTo(batch, batchSize - 1); // بقیه snapshot‌ها تا تکمیل batch
                        exportBatch(batch);
                    }
                } catch (InterruptedException e) {
                    if (!running) break;
                    Thread.currentThread().interrupt();
                }
            }
        } finally {
            System.out.println("[MetricExporterWorker] stopped.");
        }
    }

    private void exportBatch(List<MetricSnapshot> batch) {
        // export واقعی، مثلاً به console، DB یا Kafka
        for (MetricSnapshot snapshot : batch) {
            System.out.println(snapshot);
        }
    }

    public void shutdown() {
        running = false;
        this.interrupt();
    }
}




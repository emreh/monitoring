package ir.myhome.agent.worker;

import ir.myhome.agent.metrics.MetricSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public final class MetricExporterWorker extends Thread {

    private final BlockingQueue<MetricSnapshot> exportQueue;
    private final int batchSize;
    private final long pollMillis;
    private volatile boolean running = true;

    public MetricExporterWorker(BlockingQueue<MetricSnapshot> exportQueue, int batchSize, long pollMillis) {
        this.exportQueue = exportQueue;
        this.batchSize = batchSize;
        this.pollMillis = pollMillis;
        setName("MetricExporterWorker");
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            while (running) {
                List<MetricSnapshot> batch = new ArrayList<>(batchSize);
                exportQueue.drainTo(batch, batchSize);

                if (!batch.isEmpty()) {
                    exportBatch(batch);
                }

                try {
                    Thread.sleep(pollMillis);
                } catch (InterruptedException e) {
                    if (!running) break;
                }
            }
        } finally {
            System.out.println("[MetricExporterWorker] stopped.");
        }
    }

    private void exportBatch(List<MetricSnapshot> batch) {
        // export واقعی، فعلاً console
        for (MetricSnapshot snapshot : batch) {
            System.out.println("[Export] " + snapshot);
        }
    }

    public void shutdown() {
        running = false;
        this.interrupt();
    }
}

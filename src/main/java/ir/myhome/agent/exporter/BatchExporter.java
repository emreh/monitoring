package ir.myhome.agent.exporter;

import ir.myhome.agent.metrics.MetricSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public final class BatchExporter implements Runnable {

    private final BlockingQueue<MetricSnapshot> exportQueue;
    private final int batchSize;
    private final long flushMillis;
    private volatile boolean running = true;

    public BatchExporter(BlockingQueue<MetricSnapshot> exportQueue, int batchSize, long flushMillis) {
        this.exportQueue = exportQueue;
        this.batchSize = batchSize;
        this.flushMillis = flushMillis;
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

                try { Thread.sleep(flushMillis); }
                catch (InterruptedException e) { if (!running) break; }
            }
        } finally {
            System.out.println("[BatchExporter] stopped.");
        }
    }

    private void exportBatch(List<MetricSnapshot> batch) {
        for (MetricSnapshot snapshot : batch) {
            System.out.println("[Export] " + snapshot);
        }
    }

    public void shutdown() {
        running = false;
    }
}

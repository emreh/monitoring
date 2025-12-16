package ir.myhome.agent.exporter;

import ir.myhome.agent.queue.SpanQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncSpanExporter<T> {

    private final SpanQueue<T> queue;
    private final int batchSize;
    private final ExecutorService worker;

    public AsyncSpanExporter(SpanQueue<T> queue, int batchSize) {
        this.queue = queue;
        this.batchSize = batchSize;
        this.worker = Executors.newSingleThreadExecutor();
    }

    public void start() {
        worker.submit(this::runLoop);
    }

    private void runLoop() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                int currentBatchSize = Math.min(queue.size(), batchSize);
                if (currentBatchSize == 0) {
                    Thread.sleep(5); // backoff نرم
                    continue;
                }

                List<T> batch = new ArrayList<>(currentBatchSize);
                for (int i = 0; i < currentBatchSize; i++) {
                    T item = queue.poll();
                    if (item == null) break;
                    batch.add(item);
                }

                if (!batch.isEmpty()) export(batch);
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    protected void export(List<T> batch) {
        // Override در تست یا Exporter واقعی
        System.out.println("exported batch size = " + batch.size());
    }

    public void stop() {
        worker.shutdownNow();
    }
}

package ir.myhome.agent.worker;

import ir.myhome.agent.metrics.MetricCollector;
import ir.myhome.agent.metrics.MetricCollectorSingleton;
import ir.myhome.agent.metrics.MetricSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public final class BatchWorker implements Runnable {

    private final BlockingQueue<MetricSnapshot> queue;

    public BatchWorker() {
        MetricCollector collector = MetricCollectorSingleton.get();
        this.queue = collector.getExportQueue();
    }

    @Override
    public void run() {
        List<MetricSnapshot> buffer = new ArrayList<>();
        try {
            while (true) {
                MetricSnapshot snapshot = queue.take();
                buffer.add(snapshot);
                queue.drainTo(buffer);
                for (MetricSnapshot s : buffer) System.out.println("[BatchWorker] " + s);
                buffer.clear();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

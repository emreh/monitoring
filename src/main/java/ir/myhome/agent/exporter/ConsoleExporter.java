package ir.myhome.agent.exporter;

import ir.myhome.agent.metrics.MetricCollectorSingleton;
import ir.myhome.agent.metrics.MetricSnapshot;

import java.util.concurrent.BlockingQueue;

public final class ConsoleExporter implements Runnable {

    private final BlockingQueue<MetricSnapshot> queue;

    public ConsoleExporter() {
        this.queue = MetricCollectorSingleton.get().getExportQueue();
    }

    @Override
    public void run() {
        try {
            while (true) {
                MetricSnapshot snapshot = queue.take();
                System.out.println("[ConsoleExporter] " + snapshot);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[ConsoleExporter] stopped");
        }
    }
}

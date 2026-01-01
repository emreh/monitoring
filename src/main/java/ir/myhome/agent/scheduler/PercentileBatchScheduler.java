package ir.myhome.agent.scheduler;

import ir.myhome.agent.collector.PercentileCollector;
import ir.myhome.agent.exporter.impl.PercentileExporter;

public final class PercentileBatchScheduler {

    private static PercentileCollector collector;
    private static Thread schedulerThread;
    private static long flushIntervalMs;

    public static void start(PercentileCollector percentileCollector, long intervalMs) {
        collector = percentileCollector;
        flushIntervalMs = intervalMs;

        if (schedulerThread != null && schedulerThread.isAlive()) return;

        schedulerThread = new Thread(() -> {
            PercentileExporter exporter = new PercentileExporter(collector, flushIntervalMs);
            Thread exporterThread = new Thread(exporter, "PercentileExporterThread");
            exporterThread.setDaemon(true);
            exporterThread.start();

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }

            exporter.shutdown();
        }, "PercentileBatchScheduler");

        schedulerThread.setDaemon(true);
        schedulerThread.start();
    }

    public static void stop() {
        if (schedulerThread != null) schedulerThread.interrupt();

        System.out.println("[PercentileBatchScheduler] stopped");
    }

    public static void record(long durationMs) {
        if (collector != null) {
            collector.record(durationMs);
        }
    }
}

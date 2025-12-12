package ir.myhome.agent.scheduler;

import ir.myhome.agent.exporter.PercentileExporter;

public final class PercentileBatchScheduler {

    private static Thread schedulerThread;
    private static PercentileExporter exporter;
    private static Thread exporterThread;

    public static void start() {
        if (schedulerThread != null && schedulerThread.isAlive()) return;

        exporter = new PercentileExporter(2000); // flush هر 2s — قابل تنظیم
        exporterThread = new Thread(exporter, "PercentileExporterThread");
        exporterThread.setDaemon(true);
        exporterThread.start();

        schedulerThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(1000L);
                }
            } catch (InterruptedException ignored) {
            } finally {
                // stop exporter
                if (exporter != null) exporter.shutdown();
                if (exporterThread != null) exporterThread.interrupt();
            }
        }, "PercentileBatchScheduler");
        schedulerThread.setDaemon(true);
        schedulerThread.start();

        System.out.println("[PercentileBatchScheduler] started");
    }

    public static void stop() {
        if (schedulerThread != null) schedulerThread.interrupt();

        if (exporter != null) exporter.shutdown();

        if (exporterThread != null) exporterThread.interrupt();
    }
}

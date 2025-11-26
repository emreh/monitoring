package ir.myhome.agent.exporter;

import java.util.List;
import java.util.concurrent.TimeUnit;

public final class SenderThread extends Thread {

    private final SpanExporter exporter;
    private final long intervalMs;
    private volatile boolean running = true;

    public SenderThread(SpanExporter exporter, long intervalMs) {
        this.exporter = exporter;
        this.intervalMs = intervalMs;
        setName("Agent-SenderThread");
        setDaemon(true);
    }

    @Override
    public void run() {
        while (running) {
            try {
                List<String> batch = exporter.drainBatch();
                if (!batch.isEmpty()) exporter.postJsonArray(batch.toArray(new String[0]));
                TimeUnit.MILLISECONDS.sleep(intervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("[SenderThread] error: " + e.getMessage());
            }
        }
    }

    public void shutdown() {
        running = false;
        interrupt();
    }
}

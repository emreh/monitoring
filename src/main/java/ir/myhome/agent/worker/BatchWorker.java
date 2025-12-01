package ir.myhome.agent.worker;

import ir.myhome.agent.core.AgentConstants;
import ir.myhome.agent.core.JsonSerializer;
import ir.myhome.agent.core.Span;
import ir.myhome.agent.exporter.Exporter;
import ir.myhome.agent.queue.SpanQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class BatchWorker implements Runnable {

    private final SpanQueue queue;
    private final Exporter exporter;
    private final int batchSize;
    private final long intervalMs;

    public BatchWorker(SpanQueue queue, Exporter exporter, int batchSize, long intervalMs) {
        this.queue = queue;
        this.exporter = exporter;
        this.batchSize = Math.max(1, batchSize);
        this.intervalMs = Math.max(10, intervalMs);
    }

    @Override
    public void run() {
        Span[] buffer = new Span[batchSize];
        List<String> toSend = new ArrayList<>(batchSize);

        while (true) {
            try {
                int taken = queue.drainTo(buffer, batchSize);
                if (taken == 0) {
                    // sleep a bit
                    try {
                        TimeUnit.MILLISECONDS.sleep(intervalMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    continue;
                }

                toSend.clear();
                for (int i = 0; i < taken; i++) {
                    Span s = buffer[i];
                    if (s != null) toSend.add(JsonSerializer.toJson(s));
                    buffer[i] = null;
                }

                if (!toSend.isEmpty()) {
                    try {
                        exporter.export(toSend);
                        if (AgentConstants.DEBUG)
                            System.out.println("[BatchWorker] exported batch size=" + toSend.size());
                    } catch (Throwable t) {
                        System.err.println("[BatchWorker] exporter failed: " + t.getMessage());
                    }
                }
            } catch (Throwable t) {
                System.err.println("[BatchWorker] worker loop error: " + t.getMessage());

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}

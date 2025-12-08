package ir.myhome.agent.worker;

import ir.myhome.agent.config.AgentContext;
import ir.myhome.agent.exporter.Exporter;
import ir.myhome.agent.metrics.AgentMetrics;
import ir.myhome.agent.queue.SpanQueue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BatchWorker implements Runnable {

    private final SpanQueue q;
    private final Exporter exporter;
    private final int batchSize;
    private final int pollMillis;
    private final AgentMetrics metrics;

    public BatchWorker(SpanQueue q, Exporter exporter, int batchSize, int pollMillis, AgentMetrics metrics) {
        this.q = q;
        this.exporter = exporter;
        this.batchSize = Math.max(1, batchSize);
        this.pollMillis = Math.max(10, pollMillis);
        this.metrics = metrics;
    }

    @Override
    public void run() {
        List<Object> buffer = new ArrayList<>(batchSize);
        while (true) {
            try {
                // blocking wait for first item
                Object first = q.take(); // blocking
                if (first != null) buffer.add(first);

                // drain up to batchSize-1 more items without blocking
                for (int i = 1; i < batchSize; i++) {
                    Object x = q.poll();
                    if (x == null) break;
                    buffer.add(x);
                }

                // update queue size metric (best-effort)
                if (metrics != null) {
                    metrics.setQueueSize(q.size());
                }

                if (!buffer.isEmpty()) {
                    if (AgentContext.getAgentConfig().debug) {
                        System.out.println("[BatchWorker] exporting batch of size=" + buffer.size());
                    }

                    for (Object item : buffer) {
                        try {
                            Map<String, Object> m = toMap(item);
                            exporter.export(m);
                            if (AgentContext.getAgentConfig().debug) {
                                System.out.println("[BatchWorker] exported span: " + m);
                            }
                        } catch (Throwable t) {
                            if (AgentContext.getAgentConfig().debug) {
                                System.err.println("[BatchWorker] export failed: " + t.getMessage());
                            }
                        }
                    }

                    // metrics: flushed count + lastFlush
                    if (metrics != null) metrics.addFlushed(buffer.size());

                    buffer.clear();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break; // exit loop to allow graceful shutdown
            } catch (Throwable t) {
                if (AgentContext.getAgentConfig().debug) {
                    System.err.println("[BatchWorker] loop error: " + t.getMessage());
                }

                try {
                    Thread.sleep(pollMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // before exit, try to flush remaining items (best-effort)
        try {
            List<Object> remaining = new ArrayList<>();
            Object x;
            while ((x = q.poll()) != null) remaining.add(x);

            if (!remaining.isEmpty()) {
                for (Object item : remaining) {
                    try {
                        exporter.export(toMap(item));
                    } catch (Throwable ignored) {
                    }
                }

                if (metrics != null) metrics.addFlushed(remaining.size());
            }
        } catch (Throwable ignore) {
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object spanObj) {
        if (spanObj instanceof Map) return (Map<String, Object>) spanObj;

        Map<String, Object> m = new HashMap<>();
        m.put("span", spanObj);
        return m;
    }
}

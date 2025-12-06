package ir.myhome.agent.worker;

import ir.myhome.agent.config.AgentContext;
import ir.myhome.agent.exporter.Exporter;
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

    public BatchWorker(SpanQueue q, Exporter exporter, int batchSize, int pollMillis) {
        this.q = q;
        this.exporter = exporter;
        this.batchSize = Math.max(1, batchSize);
        this.pollMillis = Math.max(10, pollMillis);
    }

    @Override
    public void run() {
        List<Object> buffer = new ArrayList<>(batchSize);
        while (true) {
            try {
                Object s = q.take();
                if (s != null) buffer.add(s);
                q.poll();

                while (buffer.size() < batchSize) {
                    Object x = q.poll();
                    if (x == null) break;
                    buffer.add(x);
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
                    buffer.clear();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
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
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object spanObj) {
        if (spanObj instanceof Map) return (Map<String, Object>) spanObj;
        Map<String, Object> m = new HashMap<>();
        m.put("span", spanObj);
        return m;
    }
}

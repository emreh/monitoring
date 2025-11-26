package ir.myhome.agent.exporter;

import ir.myhome.agent.core.Span;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public final class SpanExporter {

    private final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
    private final AtomicInteger size = new AtomicInteger(0);
    private final int capacity;
    private final int batchSize;
    private final SpanExporterBackend backend;
    private final AtomicInteger dropped = new AtomicInteger(0);

    public SpanExporter(int capacity, int batchSize, String collectorUrl, SpanExporterBackend backend) {
        this.capacity = Math.max(1, capacity);
        this.batchSize = Math.max(1, batchSize);
        this.backend = backend;
    }

    public void export(Span s) {
        if (s == null) return;

        String json = JsonSerializer.toJson(s);

        while (true) {
            int cur = size.get();
            if (cur >= capacity) {
                String polled = queue.poll();
                if (polled != null) {
                    size.decrementAndGet();
                    dropped.incrementAndGet();
                } else break;
            } else {
                if (size.compareAndSet(cur, cur + 1)) {
                    queue.add(json);
                    return;
                }
            }
        }
    }

    public List<String> drainBatch() {
        List<String> out = new ArrayList<>(batchSize);

        for (int i = 0; i < batchSize; i++) {
            String v = queue.poll();
            if (v == null) break;
            out.add(v);
            size.decrementAndGet();
        }

        return out;
    }

    public void postJsonArray(String[] items) {
        if (items == null || items.length == 0) return;

        List<String> list = new ArrayList<>(items.length);

        for (String s : items) list.add(s);

        try {
            backend.exportBatch(list);
        } catch (Exception e) {
            System.err.println("[SpanExporter] backend failed: " + e.getMessage());
        }
    }

    public int getDropped() {
        return dropped.get();
    }
}

package ir.myhome.agent.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public final class SpanExporter {

    private final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
    private final int capacity;
    private final int batchSize;
    private final String collectorUrl;

    // متریک‌ها
    private final AtomicLong dropped = new AtomicLong(0);
    private final AtomicLong sent = new AtomicLong(0);
    private final AtomicLong sendErrors = new AtomicLong(0);

    // فقط یک Thread اجازه drain دارد
    private final AtomicBoolean draining = new AtomicBoolean(false);

    public SpanExporter(int capacity, int batchSize, String collectorUrl) {
        this.capacity = capacity;
        this.batchSize = batchSize;
        this.collectorUrl = collectorUrl;
    }

    public void export(Span s) {
        if (s == null) return;

        String json = JsonSerializer.toJson(s);

        if (queue.size() < capacity) {
            queue.add(json);
        } else {
            queue.poll();
            queue.add(json);
            dropped.incrementAndGet();
        }
    }

    public List<String> drainBatch() {
        if (!draining.compareAndSet(false, true)) return List.of(); // هم‌زمانی جلوگیری شد

        try {
            List<String> out = new ArrayList<>(batchSize);
            for (int i = 0; i < batchSize; i++) {
                String v = queue.poll();
                if (v == null) break;
                out.add(v);
            }
            return out;
        } finally {
            draining.set(false);
        }
    }

    public void postJsonArray(String[] items) {
        if (items == null || items.length == 0) return;

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < items.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(items[i]);
            }
            sb.append("]");

            System.out.println("[SpanExporter] POST -> " + collectorUrl);
            System.out.println(sb);

            sent.addAndGet(items.length);
        } catch (Exception ex) {
            sendErrors.incrementAndGet();
            System.err.println("SpanExporter ERROR: " + ex.getMessage());
        }
    }

    // متریک‌ها برای مرحله ۲
    public long getDroppedCount() {
        return dropped.get();
    }

    public long getSentCount() {
        return sent.get();
    }

    public long getErrorCount() {
        return sendErrors.get();
    }
}

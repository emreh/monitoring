package ir.myhome.agent.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class SpanExporter {

    private final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
    private final int capacity;
    private final int batchSize;
    private final String collectorUrl; // در این نسخه نمونه است

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
            // drop oldest (simple policy)
            queue.poll();
            queue.add(json);
        }
    }

    public List<String> drainBatch() {
        List<String> out = new ArrayList<>(batchSize);

        for (int i = 0; i < batchSize; i++) {
            String v = queue.poll();
            if (v == null) break;
            out.add(v);
        }

        return out;
    }

    // نمونه ارسال: اینجا فقط چاپ می‌کنیم. در production باید HTTP POST شود.
    public void postJsonArray(String[] items) {
        if (items == null || items.length == 0) return;

        // ساخت آرایهٔ json ساده
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int i = 0; i < items.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(items[i]);
        }

        sb.append("]");
        System.out.println("[SpanExporter] POST to " + collectorUrl + " payload=" + sb.toString());
    }
}

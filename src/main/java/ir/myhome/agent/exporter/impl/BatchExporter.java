package ir.myhome.agent.exporter.impl;

import ir.myhome.agent.core.Aggregator;
import ir.myhome.agent.core.Span;
import ir.myhome.agent.exporter.AgentExporter;
import ir.myhome.agent.queue.SpanQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BatchExporter implements AgentExporter {

    private final SpanQueue<Span> queue;
    private final int batchSize;
    private final ScheduledExecutorService scheduler;
    private final List<AgentExporter> exporters = new ArrayList<>();
    private final Aggregator aggregator;  // استفاده از Aggregator برای پردازش داده‌ها

    public static volatile boolean muteMode = false;

    // سازنده برای انتخاب چندین اکسپورتر
    public BatchExporter(SpanQueue<Span> queue, int batchSize, long intervalMs, List<AgentExporter> selectedExporters, Aggregator aggregator) {
        this.queue = queue;
        this.batchSize = batchSize;
        // ذخیره‌سازی Aggregator
        this.aggregator = aggregator;

        // اضافه کردن اکسپورترها بر اساس انتخاب‌های ورودی
        this.exporters.addAll(selectedExporters);

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Agent-Batch-Worker");
            t.setDaemon(true);
            return t;
        });

        this.scheduler.scheduleAtFixedRate(this::process, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
    }

    // پردازش داده‌ها و ارسال به همه اکسپورترها
    private void process() {
        try {
            if (muteMode) {
                return;
            }

            List<Span> batch = new ArrayList<>();
            int drained = queue.drainTo(batch, batchSize);

            if (!batch.isEmpty()) {
                // استفاده از Aggregator برای محاسبه درصد
                double p50 = aggregator.getPercentile(50);  // گرفتن P50 برای داده‌ها
                System.out.println("P50 Percentile: " + p50);

                // ارسال داده‌ها به همه اکسپورترهای انتخاب‌شده
                for (AgentExporter exporter : exporters) {
                    try {
                        exporter.export(batch);
                    } catch (Exception e) {
                        System.err.println("[BatchExporter] Error in " + exporter.getClass().getSimpleName());
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void export(List<Span> batch) {
        if (batch == null || batch.isEmpty()) {
            System.out.println("[BatchExporter] No spans to export.");
            return;
        }

        for (AgentExporter exporter : exporters) {
            try {
                exporter.export(batch);  // ارسال داده‌ها به همه اکسپورترها
            } catch (Exception e) {
                System.err.println("[BatchExporter] Error in " + exporter.getClass().getSimpleName());
                e.printStackTrace();
            }
        }
    }
}


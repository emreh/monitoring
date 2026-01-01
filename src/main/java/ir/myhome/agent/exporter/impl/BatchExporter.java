package ir.myhome.agent.exporter.impl;

import ir.myhome.agent.core.Span;
import ir.myhome.agent.exporter.AgentExporter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BatchExporter {
    private final BlockingQueue<Span> queue;
    private final int batchSize;
    private final ScheduledExecutorService scheduler;
    private final List<AgentExporter> exporters = new ArrayList<>();

    // فیلد برای کنترل آنلاین (مطیع بودن)
    public static volatile boolean muteMode = false;

    public BatchExporter(BlockingQueue<Span> queue, int batchSize, long intervalMs) {
        this.queue = queue;
        this.batchSize = batchSize;

        // اضافه کردن اکسپورتر پیش‌فرض
        this.exporters.add(new ConsoleExporter());

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Agent-Batch-Worker");
            t.setDaemon(true);
            return t;
        });

        this.scheduler.scheduleAtFixedRate(this::process, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
    }

    public void addExporter(AgentExporter exporter) {
        this.exporters.add(exporter);
    }

    private void process() {
        try {
            // ۱. سیستم مطیع: اگر دستور خفه باش صادر شده، یا صف خیلی پر است، ارسال نکن
            if (muteMode) {
                return;
            }

            // ۲. برداشتن دسته‌ای از صف
            List<Span> batch = new ArrayList<>();
            queue.drainTo(batch, batchSize);

            // ۳. ارسال به تمام اکسپورترها
            if (!batch.isEmpty()) {
                for (AgentExporter exporter : exporters) {
                    try {
                        exporter.export(batch);
                    } catch (Exception e) {
                        // خرابی یک اکسپورتر نباید روی بقیه اثر بگذارد
                        System.err.println("[BatchExporter] Error in " + exporter.getClass().getSimpleName());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
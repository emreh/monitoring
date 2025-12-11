package ir.myhome.agent.exporter;

import ir.myhome.agent.collector.SpanCollector;
import ir.myhome.agent.core.Span;
import ir.myhome.agent.queue.SpanQueue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncSpanExporter {

    private final SpanQueue queue;
    private final SpanCollector collector;
    private final ExecutorService executor;

    public AsyncSpanExporter(SpanQueue queue, SpanCollector collector, int threads) {
        this.queue = queue;
        this.collector = collector;
        this.executor = Executors.newFixedThreadPool(threads);
        startWorkers(threads);
    }

    private void startWorkers(int threads) {
        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                while (true) {
                    try {
                        Span span = (Span) queue.take(); // بلوکه می‌شود تا span آماده شود
                        collector.collect(span);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}

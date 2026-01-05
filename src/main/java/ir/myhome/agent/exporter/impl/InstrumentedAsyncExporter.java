package ir.myhome.agent.exporter.impl;

import ir.myhome.agent.collector.LatencyCollector;
import ir.myhome.agent.queue.SpanQueue;

import java.util.List;

public final class InstrumentedAsyncExporter<T> extends AsyncSpanExporter<T> {

    private final LatencyCollector collector;

    public InstrumentedAsyncExporter(SpanQueue<T> queue, int batchSize, LatencyCollector collector) {
        super(queue, batchSize);
        this.collector = collector;
    }

    @Override
    protected void export(List<T> batch) {
        for (T item : batch) {
            if (item instanceof Long timestamp) {
                long micros = (System.nanoTime() - timestamp) / 1_000;
                collector.record(micros);
            }
        }
        System.out.println("exported batch size = " + batch.size());
    }
}

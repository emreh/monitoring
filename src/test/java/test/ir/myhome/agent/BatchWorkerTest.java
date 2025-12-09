package test.ir.myhome.agent;

import ir.myhome.agent.exporter.Exporter;
import ir.myhome.agent.metrics.AgentMetrics;
import ir.myhome.agent.queue.BoundedSpanQueue;
import ir.myhome.agent.queue.SpanQueue;
import ir.myhome.agent.worker.BatchWorker;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BatchWorkerTest {

    static class MockExporter implements Exporter {
        final List<Map<String, Object>> exported = Collections.synchronizedList(new ArrayList<>());

        @Override
        public void export(Map<String, Object> span) {
            exported.add(span);
        }

        @Override
        public void close() {
        }
    }

    @Test
    void batchExported() throws Exception {
        SpanQueue q = new BoundedSpanQueue(100);
        MockExporter exporter = new MockExporter();
        AgentMetrics metrics = new AgentMetrics();
        BatchWorker worker = new BatchWorker(q, exporter, 5, 10, metrics);
        Thread t = new Thread(worker);
        t.start();

        // push 7 items
        for (int i = 0; i < 7; i++) q.offer("x" + i);
        // give worker time to flush
        Thread.sleep(200);

        // interrupt and join
        t.interrupt();
        t.join(1000);

        assertTrue(exporter.exported.size() >= 7, "should have exported all items (or at least 7)");
    }
}

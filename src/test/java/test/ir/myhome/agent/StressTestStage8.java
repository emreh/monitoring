package test.ir.myhome.agent;

import ir.myhome.agent.collector.LatencyCollector;
import ir.myhome.agent.snapshot.WindowSnapshot;

public class StressTestStage8 {

    public static void main(String[] args) throws Exception {
        LatencyCollector c = new LatencyCollector(1_000_000, 3);

        for (int i = 0; i < 10_000; i++) {
            c.record(1000 + i);
        }

        WindowSnapshot s = c.snapshot();

        assert s.count() == 10_000;

// بعد از snapshot نباید اثر بگذارد
        c.record(999999);

        WindowSnapshot s2 = null;
        try {
            s2 = c.snapshot();
            throw new AssertionError("second snapshot should fail");
        } catch (IllegalStateException expected) {
        }
    }
}

package ir.myhome.agent.collector;

import ir.myhome.agent.metrics.PercentileSample;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Collector for percentile samples. Thread-safe.
 * Public API:
 *  - record(service, endpoint, durationMs)
 *  - drainSnapshot() -> returns list of samples currently in queue (and removes them)
 */
public final class PercentileCollector {

    private static final BlockingQueue<PercentileSample> queue = new LinkedBlockingQueue<>();

    private PercentileCollector() {}

    public static void record(String service, String endpoint, long durationMs) {
        PercentileSample s = new PercentileSample(service, endpoint, durationMs, System.currentTimeMillis());
        queue.offer(s);
    }

    /**
     * Drains up to all currently queued samples into a list and returns it.
     * Caller becomes owner of the returned list.
     */
    public static List<PercentileSample> drainSnapshot() {
        List<PercentileSample> list = new ArrayList<>();
        queue.drainTo(list);
        return list;
    }

    /** only for debugging / inspection (non-destructive) */
    public static List<PercentileSample> peekSnapshot() {
        return new ArrayList<>(queue);
    }

    public static int queueSize() {
        return queue.size();
    }
}

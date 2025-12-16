package ir.myhome.agent.snapshot;

public record WindowSnapshot(long count, long p50, long p90, long p99, long hdrP99) {
}

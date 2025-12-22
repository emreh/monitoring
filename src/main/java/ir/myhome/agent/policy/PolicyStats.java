package ir.myhome.agent.policy;

import ir.myhome.agent.policy.contract.DecisionType;

import java.util.concurrent.atomic.LongAdder;

public final class PolicyStats {

    private final LongAdder allow = new LongAdder();
    private final LongAdder drop = new LongAdder();
    private final LongAdder sample = new LongAdder();
    private final LongAdder error = new LongAdder();

    public void record(DecisionType type) {
        switch (type) {
            case ALLOW -> allow.increment();
            case DROP -> drop.increment();
            case SAMPLE -> sample.increment();
        }
    }

    public void recordError() {
        error.increment();
    }

    public long allowCount() {
        return allow.sum();
    }

    public long dropCount() {
        return drop.sum();
    }

    public long sampleCount() {
        return sample.sum();
    }

    public long errorCount() {
        return error.sum();
    }
}

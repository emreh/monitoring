package ir.myhome.agent.policy;

import ir.myhome.agent.policy.contract.Decision;
import ir.myhome.agent.policy.contract.PolicyEngine;
import ir.myhome.agent.policy.contract.PolicyInput;
import ir.myhome.agent.policy.contract.ReasonCode;

public final class ReferencePolicy implements PolicyEngine {

    private final int softSampleRate; // مثلاً 10 یعنی 1 از 10

    public ReferencePolicy(int softSampleRate) {
        if (softSampleRate <= 0) {
            throw new IllegalArgumentException("softSampleRate must be > 0");
        }
        this.softSampleRate = softSampleRate;
    }

    @Override
    public Decision evaluate(PolicyInput input) {
        return switch (input.overloadState()) {
            case HARD -> Decision.drop(ReasonCode.OVERLOAD_HARD);
            case SOFT -> {
                if (shouldSample(input.traceId())) yield Decision.sample(ReasonCode.OVERLOAD_SOFT);

                yield Decision.drop(ReasonCode.OVERLOAD_SOFT);
            }
            default -> Decision.allow(ReasonCode.OK);
        };
    }

    private boolean shouldSample(long traceId) {
        // deterministic sampling, zero allocation
        return Math.floorMod(traceId, softSampleRate) == 0;
    }
}

package ir.myhome.agent.policy.contract;

public final class PolicyInput {

    private final long traceId;
    private final OverloadState overloadState;

    public PolicyInput(long traceId, OverloadState overloadState) {
        this.traceId = traceId;
        this.overloadState = overloadState;
    }

    public long traceId() {
        return traceId;
    }

    public OverloadState overloadState() {
        return overloadState;
    }
}

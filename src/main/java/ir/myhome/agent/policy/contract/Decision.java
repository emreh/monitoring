package ir.myhome.agent.policy.contract;

public final class Decision {

    private final DecisionType type;
    private final ReasonCode reason;

    private Decision(DecisionType type, ReasonCode reason) {
        this.type = type;
        this.reason = reason;
    }

    public static Decision allow(ReasonCode reason) {
        return new Decision(DecisionType.ALLOW, reason);
    }

    public static Decision drop(ReasonCode reason) {
        return new Decision(DecisionType.DROP, reason);
    }

    public static Decision sample(ReasonCode reason) {
        return new Decision(DecisionType.SAMPLE, reason);
    }

    public DecisionType type() {
        return type;
    }

    public ReasonCode reason() {
        return reason;
    }
}

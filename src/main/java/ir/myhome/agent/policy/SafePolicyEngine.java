package ir.myhome.agent.policy;

import ir.myhome.agent.policy.contract.Decision;
import ir.myhome.agent.policy.contract.PolicyEngine;
import ir.myhome.agent.policy.contract.PolicyInput;
import ir.myhome.agent.policy.contract.ReasonCode;

public final class SafePolicyEngine implements PolicyEngine {

    private final PolicyEngine delegate;
    private final PolicyStats stats;

    public SafePolicyEngine(PolicyEngine delegate, PolicyStats stats) {
        this.delegate = delegate;
        this.stats = stats;
    }

    @Override
    public Decision evaluate(PolicyInput input) {
        try {
            Decision decision = delegate.evaluate(input);
            stats.record(decision.type());
            return decision;
        } catch (Throwable t) {
            stats.recordError();
            return Decision.allow(ReasonCode.POLICY_ERROR);
        }
    }
}

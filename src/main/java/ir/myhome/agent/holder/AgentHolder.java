package ir.myhome.agent.holder;

import ir.myhome.agent.policy.PolicyStats;
import ir.myhome.agent.policy.ReferencePolicy;
import ir.myhome.agent.policy.SafePolicyEngine;
import ir.myhome.agent.policy.contract.PolicyEngine;

public final class AgentHolder {

    private static volatile PolicyEngine policyEngine;
    private static volatile PolicyStats policyStats;

    private AgentHolder() {
    }

    public static void initPolicy() {
        PolicyStats stats = new PolicyStats();
        PolicyEngine ref = new ReferencePolicy(10);
        policyEngine = new SafePolicyEngine(ref, stats);
        policyStats = stats;
    }

    public static PolicyEngine getPolicyEngine() {
        return policyEngine;
    }

    public static PolicyStats getPolicyStats() {
        return policyStats;
    }
}

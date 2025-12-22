package ir.myhome.agent.policy.contract;

public interface PolicyEngine {

    Decision evaluate(PolicyInput input);
}

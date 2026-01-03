package ir.myhome.agent.policy;

import ir.myhome.agent.policy.contract.Decision;
import ir.myhome.agent.policy.contract.PolicyEngine;
import ir.myhome.agent.policy.contract.PolicyInput;
import ir.myhome.agent.policy.contract.ReasonCode;

public final class SafePolicyEngine implements PolicyEngine {

    private final PolicyEngine delegate;
    private final PolicyStats stats;

    // سازنده که یک PolicyEngine و PolicyStats را می‌پذیرد
    public SafePolicyEngine(PolicyEngine delegate, PolicyStats stats) {
        this.delegate = delegate;
        this.stats = stats;
    }

    @Override
    public Decision evaluate(PolicyInput input) {
        try {
            // انجام ارزیابی سیاست از PolicyEngine اصلی
            Decision decision = delegate.evaluate(input);

            // ثبت تصمیمات در PolicyStats
            stats.record(decision.type());

            // برگرداندن تصمیم
            return decision;
        } catch (Throwable t) {
            // ثبت خطا در صورت وقوع استثنا
            stats.recordError();

            // بازگشت به تصمیم allow در صورت خطا
            return Decision.allow(ReasonCode.POLICY_ERROR);
        }
    }
}

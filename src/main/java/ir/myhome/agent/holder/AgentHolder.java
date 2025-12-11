package ir.myhome.agent.holder;

import ir.myhome.agent.feature.FeatureFlagManager;
import ir.myhome.agent.queue.SpanQueue;

public final class AgentHolder {

    private static volatile FeatureFlagManager featureFlagManager;
    private static volatile SpanQueue spanQueue;

    private AgentHolder() {
    }

    public static void setFeatureFlagManager(FeatureFlagManager ffm) {
        featureFlagManager = ffm;
    }

    public static FeatureFlagManager getFeatureFlagManager() {
        return featureFlagManager;
    }

    public static void setSpanQueue(SpanQueue q) {
        spanQueue = q;
    }

    public static SpanQueue getSpanQueue() {
        return spanQueue;
    }
}

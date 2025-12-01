package ir.myhome.agent.config;

import java.util.HashMap;
import java.util.Map;

public final class FeatureFlagConfig {
    private final Map<String, Boolean> global = new HashMap<>();

    public void setGlobal(String feature, boolean enabled) {
        global.put(feature, enabled);
    }

    public Boolean getGlobal(String feature) {
        return global.get(feature);
    }
}

package ir.myhome.agent.feature;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FeatureFlagManager {

    private final Map<String, Boolean> global = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Boolean>> perProject = new ConcurrentHashMap<>();

    public void setGlobal(String feature, boolean enabled) {
        global.put(feature, enabled);
    }

    public Boolean getGlobal(String feature) {
        return global.get(feature);
    }

    public boolean isEnabled(String feature, String projectId) {
        if (projectId != null) {
            Map<String, Boolean> m = perProject.get(projectId);
            if (m != null && m.containsKey(feature)) return m.get(feature);
        }
        Boolean g = getGlobal(feature);
        return g != null ? g : false;
    }

    public void setProjectOverride(String projectId, String feature, boolean enabled) {
        perProject.computeIfAbsent(projectId, k -> new ConcurrentHashMap<>()).put(feature, enabled);
    }
}

package ir.myhome.agent.config;

public final class AgentContext {
    private static volatile AgentConfig config;

    public static void init(AgentConfig cfg) {
        if (config != null) {
            return; // یا می‌توانی IllegalStateException بندازی
        }
        config = cfg;
    }

    public static AgentConfig getAgentConfig() {
        if (config == null) {
            throw new IllegalStateException("AgentContext not initialized");
        }
        return config;
    }
}


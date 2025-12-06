package ir.myhome.agent.config;

public final class AgentContext {
    private static AgentConfig config;

    public static void init(AgentConfig cfg) {
        config = cfg;
    }

    public static AgentConfig getAgentConfig() {
        return config;
    }
}


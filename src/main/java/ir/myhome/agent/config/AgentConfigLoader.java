package ir.myhome.agent.config;

public final class AgentConfigLoader {

    private static final String CONFIG_FILE = "agent-config.yml";

    private AgentConfigLoader() {}

    public static AgentConfig load() {
        AgentConfig cfg = YamlUtil.loadYaml(CONFIG_FILE, AgentConfig.class);

        if (cfg == null) {
            System.err.println("[AgentConfigLoader] using DEFAULT config (yaml missing or invalid)");
            return new AgentConfig();
        }

        System.out.println("[AgentConfigLoader] yaml config loaded successfully");
        return cfg;
    }
}

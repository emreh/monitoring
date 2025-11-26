package ir.myhome.agent.bootstrap;

import ir.myhome.agent.config.AgentConfig;
import ir.myhome.agent.config.YamlUtil;
import ir.myhome.agent.instrumentation.InstrumentationInstaller;

import java.lang.instrument.Instrumentation;

public final class AgentMain {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("[AgentMain] starting agent...");

        AgentConfig cfg = YamlUtil.loadAgentConfig("agent-config.yml");
        System.out.println("[AgentMain] config loaded: " + cfg);

        try {
            InstrumentationInstaller.install(inst, cfg);
        } catch (Throwable t) {
            System.err.println("[AgentMain] installation failed: " + t.getMessage());
            t.printStackTrace();
        }

        System.out.println("[AgentMain] premain finished");
    }
}

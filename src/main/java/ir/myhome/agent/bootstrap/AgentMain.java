package ir.myhome.agent.bootstrap;

import ir.myhome.agent.config.AgentConfig;
import ir.myhome.agent.config.YamlUtil;
import ir.myhome.agent.instrumentation.InstrumentationInstaller;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.URISyntaxException;
import java.util.jar.JarFile;

public final class AgentMain {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("[AgentMain] starting agent...");

        // load config from resource inside agent JAR
        AgentConfig cfg = YamlUtil.loadAgentConfig("agent-config.yml");
        System.out.println("[AgentMain] config loaded: " + cfg);

        // locate the agent jar file (the jar that contains this class)
        String agentJarPath = null;
        try {
            File codeSource = new File(AgentMain.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            agentJarPath = codeSource.getAbsolutePath();
            System.out.println("[AgentMain] detected agent jar: " + agentJarPath);
        } catch (URISyntaxException e) {
            System.err.println("[AgentMain] failed to detect agent jar: " + e.getMessage());
        }

        // append agent jar to bootstrap classloader search so helper classes are visible to bootstrap targets
        if (agentJarPath != null) {
            try {
                JarFile jf = new JarFile(agentJarPath);
                inst.appendToBootstrapClassLoaderSearch(jf);
                System.out.println("[AgentMain] appended agent JAR to bootstrap classloader search");
            } catch (Throwable t) {
                System.err.println("[AgentMain] failed to appendToBootstrapClassLoaderSearch: " + t.getMessage());
                t.printStackTrace();
            }
        } else {
            System.err.println("[AgentMain] WARNING: agentJarPath==null, bootstrap injection skipped (will likely fail for JDK instrumentation)");
        }

        // install instrumentation (pass instrumentation and cfg)
        try {
            InstrumentationInstaller.install(inst, cfg);
        } catch (Throwable t) {
            System.err.println("[AgentMain] InstrumentationInstaller.install threw:");
            t.printStackTrace();
        }
    }
}

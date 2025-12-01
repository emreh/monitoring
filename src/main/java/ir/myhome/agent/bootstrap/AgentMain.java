package ir.myhome.agent.bootstrap;

import ir.myhome.agent.config.AgentConfig;
import ir.myhome.agent.config.YamlUtil;
import ir.myhome.agent.instrumentation.InstrumentationInstaller;
import ir.myhome.agent.queue.BoundedSpanQueue;
import ir.myhome.agent.worker.BatchWorker;
import ir.myhome.agent.exporter.ConsoleExporter;
import ir.myhome.agent.exporter.HttpExporter;
import ir.myhome.agent.feature.FeatureFlagManager;
import ir.myhome.agent.core.AgentConstants;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class AgentMain {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("[AgentMain] starting agent...");

        AgentConfig cfg = YamlUtil.loadAgentConfig("agent-config.yml");
        System.out.println("[AgentMain] config loaded: " + cfg);

        // feature flags
        FeatureFlagManager ffm = new FeatureFlagManager();
        AgentHolder.setFeatureFlagManager(ffm);

        // queue
        BoundedSpanQueue q = new BoundedSpanQueue(cfg.exporterCapacity);
        AgentHolder.setSpanQueue(q);

        // exporter
        if ("http".equalsIgnoreCase(cfg.exporterType)) {
            AgentHolder.setExporter(new HttpExporter(cfg.exporterEndpoint, cfg.exporterBatchSize));
        } else {
            AgentHolder.setExporter(new ConsoleExporter());
        }

        // start batch worker (single thread)
        BatchWorker worker = new BatchWorker(q, AgentHolder.getExporter(), cfg.exporterBatchSize, 200);
        Thread t = new Thread(worker, "agent-batch-worker");
        t.setDaemon(true);
        t.start();

        // install instrumentation
        try {
            InstrumentationInstaller.install(inst, cfg);
            System.out.println("[AgentMain] instrumentation installed");
        } catch (Throwable tEx) {
            System.err.println("[AgentMain] instrumentation install failed: " + tEx.getMessage());
            tEx.printStackTrace();
        }

        if (AgentConstants.DEBUG) {
            System.out.println("[AgentMain] agent started (debug ON)");
        } else {
            System.out.println("[AgentMain] agent started");
        }
    }
}

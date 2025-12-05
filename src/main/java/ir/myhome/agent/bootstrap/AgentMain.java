package ir.myhome.agent.bootstrap;

import ir.myhome.agent.config.AgentConfig;
import ir.myhome.agent.config.YamlUtil;
import ir.myhome.agent.core.AgentContext;
import ir.myhome.agent.exporter.ConsoleExporter;
import ir.myhome.agent.exporter.Exporter;
import ir.myhome.agent.exporter.HttpExporter;
import ir.myhome.agent.instrumentation.InstrumentationInstaller;
import ir.myhome.agent.queue.BoundedSpanQueue;
import ir.myhome.agent.queue.SpanQueue;
import ir.myhome.agent.worker.BatchWorker;

import java.lang.instrument.Instrumentation;

public final class AgentMain {

    public static void premain(String args, Instrumentation inst) {
        System.out.println("[AgentMain] starting agent...");

        // load config from YAML
        AgentConfig cfg = YamlUtil.loadYaml("agent-config.yml", AgentConfig.class);
        if (cfg == null) throw new RuntimeException("Agent config not loaded!");

        // initialize context
        AgentContext.init(cfg);

        // setup queue
        SpanQueue queue = new BoundedSpanQueue(AgentContext.getAgentConfig().exporter.batchSize);

        // setup exporter
        Exporter exporter;
        if ("http".equalsIgnoreCase(AgentContext.getAgentConfig().exporter.type)) {
            exporter = new HttpExporter(AgentContext.getAgentConfig().exporter.endpoint);
        } else {
            exporter = new ConsoleExporter();
        }

        // start batch worker
        Thread workerThread = new Thread(new BatchWorker(queue, exporter, AgentContext.getAgentConfig().exporter.batchSize, 20), "BatchWorkerThread");
        workerThread.setDaemon(true);
        workerThread.start();

        // install instrumentation
        InstrumentationInstaller.install(inst, AgentContext.getAgentConfig());

        System.out.println("[AgentMain] agent started");
    }
}

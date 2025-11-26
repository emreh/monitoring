package ir.myhome.agent.instrumentation;

import ir.myhome.agent.backend.ConsoleBackend;
import ir.myhome.agent.backend.HttpBackend;
import ir.myhome.agent.bootstrap.ExporterHolder;
import ir.myhome.agent.config.AgentConfig;
import ir.myhome.agent.exporter.SpanExporter;
import ir.myhome.agent.exporter.SpanExporterBackend;
import ir.myhome.agent.instrumentation.advice.ExecutorServiceAdvice;
import ir.myhome.agent.instrumentation.advice.TimingAdvice;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;

public final class InstrumentationInstaller {

    private InstrumentationInstaller() {}

    public static void install(Instrumentation inst, AgentConfig cfg) {
        System.out.println("[InstrumentationInstaller] installing...");

        SpanExporterBackend backend = switch (cfg.exporterType) {
            case "http" -> new HttpBackend(cfg.exporterEndpoint);
            case "console" -> new ConsoleBackend();
            default -> new ConsoleBackend();
        };

        SpanExporter exporter = new SpanExporter(cfg.exporterCapacity, cfg.exporterBatchSize, cfg.exporterEndpoint, backend);
        ExporterHolder.setExporter(exporter);

        Thread sender = new Thread(() -> {
            while (true) {
                try {
                    List<String> batch = exporter.drainBatch();
                    if (!batch.isEmpty()) exporter.postJsonArray(batch.toArray(new String[0]));
                    Thread.sleep(Math.max(100, cfg.exporterBatchSize));
                } catch (Throwable t) {
                    System.err.println("[InstrumentationInstaller] sender error: " + t.getMessage());
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                }
            }
        }, "agent-span-sender");
        sender.setDaemon(true);
        sender.start();

        AgentBuilder.Listener listener = new AgentBuilder.Listener.Adapter() {
            @Override
            public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded, Throwable throwable) {
                System.err.println("[Agent ERROR] type=" + typeName + " : " + throwable);
            }
        };

        AgentBuilder builder = new AgentBuilder.Default()
                .with(listener)
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
                .ignore(nameStartsWith("net.bytebuddy."))
                .ignore(nameStartsWith("org.springframework."))
                .ignore(nameStartsWith("com.fasterxml.jackson."));

        if (cfg.isInstrumentationApp()) {
            try {
                builder.type(nameStartsWith("ir.myhome.spring"))
                        .transform((b, td, cl, m, pd) ->
                                b.visit(net.bytebuddy.asm.Advice.to(TimingAdvice.class)
                                        .on(isMethod().and(not(isConstructor())).and(not(isAbstract())))))
                        .installOn(inst);

                System.out.println("[InstrumentationInstaller] installed app-timing for ir.myhome.spring");
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        if (cfg.isInstrumentationExecutor()) {
            try {
                new AgentBuilder.Default()
                        .ignore(nameStartsWith("net.bytebuddy."))
                        .type(nameStartsWith("ir.myhome.spring"))
                        .transform(new ExecutorTransformer())
                        .installOn(inst);

                new AgentBuilder.Default()
                        .with(listener)
                        .ignore(nameStartsWith("net.bytebuddy."))
                        .type(nameStartsWith("ir.myhome.spring"))
                        .transform((b, td, cl, m, pd) ->
                                b.visit(net.bytebuddy.asm.Advice.to(ExecutorServiceAdvice.class)
                                        .on(named("submit").and(takesArguments(1)).and(takesArgument(0, java.util.concurrent.Callable.class)))))
                        .installOn(inst);

                System.out.println("[InstrumentationInstaller] installed executor instrumentation (app-local)");
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        System.out.println("[InstrumentationInstaller] finished install phase");
    }
}

package ir.myhome.agent.instrumentation;

import ir.myhome.agent.backend.ConsoleBackend;
import ir.myhome.agent.backend.HttpBackend;
import ir.myhome.agent.bootstrap.ExporterHolder;
import ir.myhome.agent.config.AgentConfig;
import ir.myhome.agent.exporter.SpanExporter;
import ir.myhome.agent.exporter.SpanExporterBackend;
import ir.myhome.agent.instrumentation.advice.ExecutorServiceAdvice;
import ir.myhome.agent.instrumentation.advice.ExecutorTraceAdvice;
import ir.myhome.agent.instrumentation.advice.TimingAdvice;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;

public final class InstrumentationInstaller {

    private InstrumentationInstaller() {}

    public static void install(Instrumentation inst, AgentConfig cfg) {
        System.out.println("[InstrumentationInstaller] installing...");

        // backend
        SpanExporterBackend backend = switch (cfg.exporterType) {
            case "http" -> new HttpBackend(cfg.exporterEndpoint);
            case "console" -> new ConsoleBackend();
            default -> new ConsoleBackend();
        };

        SpanExporter exporter = new SpanExporter(
                cfg.exporterCapacity,
                cfg.exporterBatchSize,
                cfg.exporterEndpoint,
                backend
        );

        ExporterHolder.setExporter(exporter);

        // sender thread
        Thread sender = new Thread(() -> {
            while (true) {
                try {
                    List<String> batch = exporter.drainBatch();
                    if (!batch.isEmpty()) {
                        exporter.postJsonArray(batch.toArray(new String[0]));
                    }
                    Thread.sleep(200);
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
            public void onError(String typeName, ClassLoader classLoader,
                                JavaModule module, boolean loaded, Throwable throwable) {
                System.err.println("[Agent ERROR] type=" + typeName + " : " + throwable);
            }
        };

        AgentBuilder builder = new AgentBuilder.Default()
                .with(listener)
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .ignore(nameStartsWith("net.bytebuddy."))
                .ignore(nameStartsWith("org.springframework."))
                .ignore(nameStartsWith("com.fasterxml.jackson."))
                .with(new net.bytebuddy.ByteBuddy()
                        .with(net.bytebuddy.dynamic.scaffold.TypeValidation.DISABLED)
                );

        // 1) timing instrumentation for app
        if (cfg.isInstrumentationApp()) {
            builder = builder.type(nameStartsWith("ir.myhome.spring"))
                    .transform((b, td, cl, m, pd) ->
                            b.visit(Advice.to(TimingAdvice.class)
                                    .on(isMethod()
                                            .and(not(isAbstract()))
                                            .and(not(isConstructor()))
                                    )
                            )
                    );
        }

        // 2) ExecutorService instrumentation
        if (cfg.isInstrumentationExecutor()) {

            // instrument execute(Runnable) and submit(Runnable)
            builder = builder.type(isSubTypeOf(java.util.concurrent.ExecutorService.class))
                    .transform((b, td, cl, m, pd) ->
                            b.method(named("execute")
                                            .and(takesArguments(1))
                                            .and(takesArgument(0, Runnable.class)))
                                    .intercept(Advice.to(ExecutorTraceAdvice.class))
                                    .method(named("submit")
                                            .and(takesArguments(1))
                                            .and(takesArgument(0, Runnable.class)))
                                    .intercept(Advice.to(ExecutorTraceAdvice.class))
                    );

            // instrument submit(Callable)
            builder = builder.type(isSubTypeOf(java.util.concurrent.ExecutorService.class))
                    .transform((b, td, cl, m, pd) ->
                            b.visit(Advice.to(ExecutorServiceAdvice.class)
                                    .on(named("submit")
                                            .and(takesArguments(1))
                                            .and(takesArgument(0, java.util.concurrent.Callable.class)))
                            )
                    );
        }

        // install once
        builder.installOn(inst);

        System.out.println("[InstrumentationInstaller] finished install phase");
    }
}

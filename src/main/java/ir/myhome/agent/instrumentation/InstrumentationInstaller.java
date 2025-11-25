package ir.myhome.agent.instrumentation;

import ir.myhome.agent.backend.ConsoleBackend;
import ir.myhome.agent.backend.HttpBackend;
import ir.myhome.agent.bootstrap.ExporterHolder;
import ir.myhome.agent.config.AgentConfig;
import ir.myhome.agent.exporter.SpanExporter;
import ir.myhome.agent.instrumentation.advice.CompletableFutureAdvice;
import ir.myhome.agent.instrumentation.advice.ExecutorServiceAdvice;
import ir.myhome.agent.instrumentation.advice.ExecutorTraceAdvice;
import ir.myhome.agent.instrumentation.advice.TimingAdvice;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;

public final class InstrumentationInstaller {

    private InstrumentationInstaller() {
    }

    public static void install(Instrumentation inst, AgentConfig cfg) {
        System.out.println("[InstrumentationInstaller] installing...");

        // --- 1) build exporter + register globally ---
        var backend = switch (cfg.exporterType) {
            case "console" -> new ConsoleBackend();
            case "http" -> new HttpBackend(cfg.exporterEndpoint);
            default -> new ConsoleBackend();
        };

        SpanExporter exporter = new SpanExporter(cfg.exporterCapacity, cfg.exporterBatchSize, cfg.exporterEndpoint, backend);
        ExporterHolder.setExporter(exporter);

        // sender thread (drain + post) — keep as before but with safer pacing
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
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }, "agent-span-sender");
        sender.setDaemon(true);
        sender.start();

        // --- 2) prepare listener to capture transform errors (very important) ---
        AgentBuilder.Listener listener = new AgentBuilder.Listener() {
            @Override
            public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
            }

            @Override
            public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded, DynamicType dynamicType) {
            }

            @Override
            public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded) {
            }

            @Override
            public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
            }

            @Override
            public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded, Throwable throwable) {
                System.err.println("[Agent ERROR] transforming: " + typeName);
                throwable.printStackTrace();
            }
        };

        // --- 3) base AgentBuilder common settings ---
        AgentBuilder builder = new AgentBuilder.Default().with(listener).with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION).with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
                // avoid ByteBuddy trying to validate types strictly (helps compatibility)
                .with(new net.bytebuddy.ByteBuddy().with(net.bytebuddy.dynamic.scaffold.TypeValidation.DISABLED));

        // --- 4) Instrument application package first (safe) ---
        if (true) {
            try {
                builder = builder.ignore(nameStartsWith("net.bytebuddy.").or(nameStartsWith("jdk.")).or(nameStartsWith("sun.")).or(nameStartsWith("com.sun."))).type(nameStartsWith("ir.myhome.spring")).transform((builder1, typeDescription, classLoader, module, protectionDomain) -> builder1.visit(Advice.to(TimingAdvice.class).on(isMethod().and(not(isConstructor())).and(not(isAbstract())))));
                builder.installOn(inst);
                System.out.println("[InstrumentationInstaller] installed app-timing instrumentation for ir.myhome.spring");
            } catch (Throwable t) {
                System.err.println("[InstrumentationInstaller] failed to install app instrumentation:");
                t.printStackTrace();
            }
        }

        // --- 5) Now instrument JDK targets SAFELY (only if bootstrap injection succeeded) ---
        // IMPORTANT: we only do this if cfg flags are true; these transforms require helper classes visible to bootstrap.
        if (cfg.instrumentationExecutor) {
            try {
                new AgentBuilder.Default().with(listener).ignore(nameStartsWith("net.bytebuddy."))
                        // do not ignore java.* here because we target JDK classes explicitly
                        .type(named("java.util.concurrent.ThreadPoolExecutor")).transform((builder1, td, cl, module, pd) -> builder1.method(named("execute").or(named("submit"))).intercept(Advice.to(ExecutorTraceAdvice.class))).installOn(inst);

                new AgentBuilder.Default().with(listener).ignore(nameStartsWith("net.bytebuddy.")).type(named("java.util.concurrent.ExecutorService")).transform((builder1, td, cl, module, pd) -> builder1.method(named("submit")).intercept(Advice.to(ExecutorServiceAdvice.class))).installOn(inst);

                System.out.println("[InstrumentationInstaller] installed executor instrumentation");
            } catch (Throwable t) {
                System.err.println("[InstrumentationInstaller] failed to install executor instrumentation:");
                t.printStackTrace();
            }
        }

        if (cfg.instrumentationCompletable) {
            try {
                new AgentBuilder.Default().with(listener).ignore(nameStartsWith("net.bytebuddy.")).type(named("java.util.concurrent.CompletableFuture")).transform((builder1, td, cl, module, pd) -> builder1.method(named("supplyAsync").or(named("runAsync")).or(named("completeAsync"))).intercept(Advice.to(CompletableFutureAdvice.class))).installOn(inst);
                System.out.println("[InstrumentationInstaller] installed completable instrumentation");
            } catch (Throwable t) {
                System.err.println("[InstrumentationInstaller] failed to install completable instrumentation:");
                t.printStackTrace();
            }
        }

        if (cfg.instrumentationHttpClient) {
            try {
                new AgentBuilder.Default().with(listener).ignore(nameStartsWith("net.bytebuddy.")).type(named("java.net.http.HttpClient")).transform((builder1, td, cl, module, pd) -> builder1.method(named("send").or(named("sendAsync"))).intercept(Advice.to(TimingAdvice.class))).installOn(inst);

                new AgentBuilder.Default().with(listener).ignore(nameStartsWith("net.bytebuddy.")).type(named("java.net.HttpURLConnection")).transform((builder1, td, cl, module, pd) -> builder1.method(named("connect").or(named("getInputStream")).or(named("getOutputStream"))).intercept(Advice.to(TimingAdvice.class))).installOn(inst);

                System.out.println("[InstrumentationInstaller] installed http-client instrumentation");
            } catch (Throwable t) {
                System.err.println("[InstrumentationInstaller] failed to install httpclient instrumentation:");
                t.printStackTrace();
            }
        }

        // other instrumentation (jdbc, scheduled, reactive) can be added similarly with try/catch...
        System.out.println("[InstrumentationInstaller] finished install phase");
    }
}

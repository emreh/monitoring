package ir.myhome.agent.instrumentation;

import ir.myhome.agent.core.SpanExporter;
import ir.myhome.agent.instrumentation.advice.CompletableFutureAdvice;
import ir.myhome.agent.instrumentation.advice.ExecutorTraceAdvice;
import ir.myhome.agent.instrumentation.advice.TimingAdvice;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class AgentMain {

    private static final int EXPORTER_CAPACITY = 10000;
    private static final int BATCH_SIZE = 20;
    private static final long BATCH_INTERVAL_MS = 2000;
    private static final String COLLECTOR_URL = "http://collector:8080/spans";

    public static void premain(String agentArgs, Instrumentation inst) {

        System.out.println("[SimpleAgent] Starting agent...");

        // span exporter shared instance
        SpanExporter exporter = new SpanExporter(EXPORTER_CAPACITY, BATCH_SIZE, COLLECTOR_URL);
        TimingAdvice.exporter = exporter;
        ExecutorTraceAdvice.setExporter(exporter);

        // periodic sender
        ScheduledExecutorService sender = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "span-exporter-sender");
            t.setDaemon(true);
            return t;
        });

        sender.scheduleAtFixedRate(() -> {
            try {
                var batch = exporter.drainBatch();
                if (!batch.isEmpty()) exporter.postJsonArray(batch.toArray(new String[0]));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }, BATCH_INTERVAL_MS, BATCH_INTERVAL_MS, TimeUnit.MILLISECONDS);

        System.out.println("[SimpleAgent] Exporter initialized (batch every " + BATCH_INTERVAL_MS + " ms)");

        // instrument controllers & services (sync)
        new AgentBuilder.Default().ignore(nameStartsWith("net.bytebuddy.").or(nameStartsWith("org.springframework.boot.loader.")).or(nameStartsWith("sun.")).or(nameStartsWith("com.sun.")).or(nameStartsWith("org.springframework.")).or(nameStartsWith("jdk.")).or(nameStartsWith("java."))).type(nameStartsWith("ir.myhome.spring.controller").or(nameStartsWith("ir.myhome.spring.service"))).transform((builder, typeDescription, classLoader, module, protectionDomain) -> builder.visit(Advice.to(TimingAdvice.class).on(isMethod().and(not(isConstructor())).and(not(isAbstract()))))).installOn(inst);

        // instrument ThreadPoolExecutor.execute/submit to wrap Runnable/Callable
        new AgentBuilder.Default().ignore(nameStartsWith("net.bytebuddy.")).type(named("java.util.concurrent.ThreadPoolExecutor")).transform((builder, td, cl, module, pd) -> builder.method(named("execute").or(named("submit"))).intercept(Advice.to(ExecutorTraceAdvice.class))).installOn(inst);

        // optional: instrument CompletableFuture static async factories (may require add-opens)
        new AgentBuilder.Default().ignore(nameStartsWith("net.bytebuddy.")).type(named("java.util.concurrent.CompletableFuture")).transform((builder, td, cl, module, pd) -> builder.method(named("supplyAsync").or(named("runAsync")).or(named("completeAsync"))).intercept(Advice.to(CompletableFutureAdvice.class))).installOn(inst);

        System.out.println("[SimpleAgent] Agent installed (مرحله ۱ مانیتورینگ)");
    }
}

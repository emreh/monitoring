package ir.myhome.agent;

import ir.myhome.agent.advice.ControllerTraceAdvice;
import ir.myhome.agent.advice.ServiceTraceAdvice;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AgentMain {

    static final LinkedBlockingQueue<String> outQueue = new LinkedBlockingQueue<>(10_000);
    private static List<String> packagePrefixes = List.of("ir.myhome.spring");

    private static final ScheduledExecutorService senderExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "agent-sender");
        t.setDaemon(true);
        return t;
    });

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("Agent premain starting...");
        senderExecutor.scheduleAtFixedRate(() -> {
            while (!outQueue.isEmpty()) outQueue.poll();
        }, 1000, 1000, TimeUnit.MILLISECONDS);

        AgentBuilder agentBuilder = new AgentBuilder.Default()
                .ignore(ElementMatchers.nameStartsWith("net.bytebuddy."))
                .ignore(ElementMatchers.nameStartsWith("sun."))
                .ignore(ElementMatchers.nameStartsWith("java."))
                .ignore(ElementMatchers.nameStartsWith("jdk."))
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .with(AgentBuilder.Listener.StreamWriting.toSystemOut());

        // Controller
        agentBuilder = agentBuilder
                .type(typeDesc -> typeDesc.getName().startsWith("ir.myhome.spring.controller"))
                .transform((builder, td, cl, module, pd) ->
                        builder.visit(Advice.to(ControllerTraceAdvice.class)
                                .on(ElementMatchers.isMethod()
                                        .and(ElementMatchers.not(ElementMatchers.isConstructor()))
                                ))
                );

// Service
        agentBuilder = agentBuilder
                .type(typeDesc -> typeDesc.getName().startsWith("ir.myhome.spring.service"))
                .transform((builder, td, cl, module, pd) ->
                        builder.visit(Advice.to(ServiceTraceAdvice.class)
                                .on(ElementMatchers.isMethod()
                                        .and(ElementMatchers.not(ElementMatchers.isConstructor()))
                                ))
                );


        agentBuilder.installOn(inst);

        System.out.println("Agent installed. Monitoring packages: " + packagePrefixes);
    }

    public static void enqueueSpan(String json) {
        outQueue.offer(json);
    }
}

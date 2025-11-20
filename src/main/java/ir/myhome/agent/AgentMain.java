package ir.myhome.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.instrument.Instrumentation;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

/**
 * Minimal Java Agent (PoC)
 * - Instruments methods in packages provided (default: ir.myhome.service)
 * - Measures duration (ms) of each method invocation
 * - Batches spans and POSTs JSON array to collectorUrl
 *
 * Usage example:
 * -javaagent:/path/to/simple-agent-0.1.0.jar=collectorUrl=http://localhost:8081/api/v1/spans,packages=ir.myhome.service;com.other
 */
public class AgentMain {

    // outbound queue for JSON payloads (individual span JSONs)
    static final LinkedBlockingQueue<String> outQueue = new LinkedBlockingQueue<>(10_000);

    // default configuration
    private static String collectorUrl = "http://localhost:8081/api/v1/spans";
    private static List<String> packagePrefixes = List.of("ir.myhome.service");
    private static int batchIntervalMs = 1000; // flush interval
    private static int batchSize = 50;

    private static final ScheduledExecutorService senderExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "agent-sender");
        t.setDaemon(true);
        return t;
    });

    public static void premain(String agentArgs, Instrumentation inst) {
        try {
            System.out.println("Agent premain starting with args: " + agentArgs);
            parseArgs(agentArgs);

            // start sender runnable scheduled at fixed rate
            senderExecutor.scheduleAtFixedRate(AgentMain::flushBatch, batchIntervalMs, batchIntervalMs, TimeUnit.MILLISECONDS);

            // Install ByteBuddy transformer
            AgentBuilder agentBuilder = new AgentBuilder.Default()
                    .ignore(ElementMatchers.nameStartsWith("net.bytebuddy."))
                    .ignore(ElementMatchers.nameStartsWith("sun."))
                    .ignore(ElementMatchers.nameStartsWith("java."))
                    .ignore(ElementMatchers.nameStartsWith("jdk."))
                    .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                    .with(AgentBuilder.Listener.StreamWriting.toSystemOut());

            agentBuilder = agentBuilder.type(typeDesc -> matchesPackage(typeDesc.getName()))
                    .transform(new AgentBuilder.Transformer() {
                        @Override
                        public DynamicType.Builder<?> transform(
                                DynamicType.Builder<?> builder,
                                TypeDescription typeDescription,
                                ClassLoader classLoader,
                                JavaModule module,
                                ProtectionDomain protectionDomain) {
                            return builder.visit(Advice.to(TimingAdvice.class)
                                    .on(ElementMatchers.isMethod()
                                            .and(ElementMatchers.not(ElementMatchers.isConstructor()))
                                            .and(ElementMatchers.not(ElementMatchers.nameStartsWith("lambda$")))
                                    ));
                        }

                        // compatibility overload (do NOT annotate with @Override)
                        public DynamicType.Builder<?> transform(
                                DynamicType.Builder<?> builder,
                                TypeDescription typeDescription,
                                ClassLoader classLoader,
                                JavaModule module) {
                            return transform(builder, typeDescription, classLoader, module, null);
                        }
                    });

            agentBuilder = agentBuilder
                    .type(ElementMatchers.declaresMethod(
                            ElementMatchers.named("doFilter")
                                    .and(ElementMatchers.takesArguments(3))
                    ))
                    .transform((builder, td, cl, module, pd) ->
                            builder.visit(Advice.to(ServletFilterAdvice.class)
                                    .on(ElementMatchers.named("doFilter")
                                            .and(ElementMatchers.takesArguments(3))
                                    ))
                    );

            agentBuilder = agentBuilder
                    // target classes that look like controllers and are inside configured packages
                    .type(typeDesc -> {
                        String name = typeDesc.getName();
                        boolean isControllerName = name.contains("Controller");
                        boolean inPackage = false;

                        for (String p : packagePrefixes)
                            if (name.startsWith(p)) {
                                inPackage = true;
                                break;
                            }

                        return isControllerName && inPackage;
                    })
                    .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                            builder.visit(Advice.to(MethodTimerAdvice.class)
                                    .on(net.bytebuddy.matcher.ElementMatchers.isMethod()
                                            .and(net.bytebuddy.matcher.ElementMatchers.not(net.bytebuddy.matcher.ElementMatchers.isConstructor()))
                                    )
                            )
                    );

            agentBuilder = agentBuilder.type((type) -> type.getName().equals("ir.myhome.service.CalculatorService"))
                    .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                            builder.visit(Advice.to(CalcAdvice.class).on(
                                    named("add").and(takesArguments(2))
                            ))
                    );

            agentBuilder.installOn(inst);

            System.out.println("Agent installed. Monitoring packages: " + packagePrefixes);
        } catch (Throwable t) {
            System.err.println("Agent failed to start" + t);
        }
    }

    private static boolean matchesPackage(String className) {
        if (className == null) return false;
        for (String p : packagePrefixes) {
            if (className.startsWith(p)) return true;
        }
        return false;
    }

    private static void parseArgs(String agentArgs) {
        if (agentArgs == null || agentArgs.isBlank()) return;
        // agentArgs format: key1=val1,key2=val2
        for (String kv : agentArgs.split(",")) {
            String[] parts = kv.split("=", 2);
            if (parts.length != 2) continue;
            String k = parts[0].trim();
            String v = parts[1].trim();
            try {
                switch (k) {
                    case "collectorUrl":
                        collectorUrl = v;
                        break;
                    case "packages":
                        packagePrefixes = Arrays.asList(v.split(";"));
                        break;
                    case "batchIntervalMs":
                        batchIntervalMs = Integer.parseInt(v);
                        break;
                    case "batchSize":
                        batchSize = Integer.parseInt(v);
                        break;
                    default:
                        System.out.println("Unknown agent arg: " + k + "=" + v);
                }
            } catch (Exception e) {
                System.err.println("Invalid agent arg {}={}, ignoring" + k + "=" + v);
            }
        }
    }

    // invoked periodically to flush up to batchSize messages
    private static void flushBatch() {
        try {
            if (outQueue.isEmpty()) return;
            List<String> batch = new ArrayList<>(batchSize);
            outQueue.drainTo(batch, batchSize);
            if (batch.isEmpty()) return;

            // create a simple JSON array payload
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < batch.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(batch.get(i));
            }
            sb.append("]");
            postJson(collectorUrl, sb.toString());
        } catch (Exception e) {
            System.out.println("Error flushing batch" + e);
        }
    }

    static void postJson(String urlStr, String json) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1500);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            byte[] payload = json.getBytes(StandardCharsets.UTF_8);
            conn.setFixedLengthStreamingMode(payload.length);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload);
            }
            int code = conn.getResponseCode();
            if (code >= 200 && code < 300) {
                // ok
            } else {
                System.out.println("Collector returned HTTP " + code);
            }
        } catch (IOException e) {
            System.out.println("Failed to send to collector (dropping): " + e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    // helper for Advice to enqueue JSON
    public static void enqueueSpan(String json) {
        // offer with timeout to avoid blocking app threads
        try {
            boolean ok = outQueue.offer(json, 5, TimeUnit.MILLISECONDS);
            if (!ok) {
                // queue full -> drop oldest and try once
                outQueue.poll();
                outQueue.offer(json);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

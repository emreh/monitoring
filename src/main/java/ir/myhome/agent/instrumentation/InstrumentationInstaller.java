package ir.myhome.agent.instrumentation;

import ir.myhome.agent.config.AgentConfig;
import ir.myhome.agent.config.AgentConfig.TimingConfig;
import ir.myhome.agent.instrumentation.advice.ExecutorServiceAdvice;
import ir.myhome.agent.instrumentation.advice.JdbcAdvice;
import ir.myhome.agent.instrumentation.advice.TimingAdviceEnter;
import ir.myhome.agent.instrumentation.advice.TimingAdviceExitDynamic;
import ir.myhome.agent.util.listener.SimpleErrorListener;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.instrument.Instrumentation;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;

public final class InstrumentationInstaller {

    public static void install(Instrumentation inst, AgentConfig cfg) {
        AgentBuilder.Listener listener = new SimpleErrorListener(System.out);

        // default ignore set; avoid instrumenting framework classes
        AgentBuilder builder = new AgentBuilder.Default()
                .with(listener)
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .ignore(
                        nameStartsWith("java.")
                                .or(nameStartsWith("javax."))
                                .or(nameStartsWith("sun."))
                                .or(nameStartsWith("jdk."))
                                .or(nameStartsWith("org.apache."))
                                .or(nameStartsWith("org.springframework."))
                                .or(nameStartsWith("com.sun."))
                                .or(nameStartsWith("net.bytebuddy."))
                );

        try {
            if (cfg.instrumentation.executor.enabled) {
                builder = builder
                        .type(isSubTypeOf(java.util.concurrent.ExecutorService.class)
                                .and(nameStartsWith("ir.myhome.spring.")))
                        .transform((b, td, cl, md, pd) ->
                                b.method(named("execute")
                                                .or(named("submit"))
                                                .or(named("invokeAll"))
                                                .or(named("invokeAny")))
                                        .intercept(Advice.to(ExecutorServiceAdvice.class))
                        );
            }

            if (cfg.instrumentation.jdbc.enabled) {
                builder = builder
                        .type(nameStartsWith("ir.myhome.spring.")
                                .and(nameContains("jdbc").or(nameContains("dao")).or(nameContains("repository"))))
                        .transform((b, td, cl, md, pd) ->
                                b.method(named("execute").or(named("executeQuery")).or(named("executeUpdate")))
                                        .intercept(Advice.to(JdbcAdvice.class))
                        );
            }

            // timing instrumentation: use entrypoints defined in cfg
            if (cfg.instrumentation.timing != null && cfg.instrumentation.timing.enabled) {
                TimingConfig timing = cfg.instrumentation.timing;
                AgentBuilder matcherBuilder = builder; // start from current builder

                // build a type matcher based on entrypoints
                net.bytebuddy.matcher.ElementMatcher.Junction<?> typeMatcher = null;
                List<String> entries = timing.entrypoints;

                if (entries == null || entries.isEmpty()) {
                    // fallback: instrument whole package prefix
                    typeMatcher = nameStartsWith("ir.myhome.spring.");
                } else {
                    for (String ep : entries) {
                        if (ep == null || ep.isEmpty()) continue;
                        // support pattern like "ir.myhome.spring.controller.*"
                        String trimmed = ep.trim();
                        if (trimmed.endsWith(".*")) {
                            String prefix = trimmed.substring(0, trimmed.length() - 2);
                            if (typeMatcher == null) typeMatcher = nameStartsWith(prefix + ".");
                            else typeMatcher = typeMatcher.or(nameStartsWith(prefix + "."));
                        } else {
                            // exact package/class match or prefix without wildcard
                            if (trimmed.endsWith(".")) trimmed = trimmed.substring(0, trimmed.length() - 1);
                            if (typeMatcher == null) typeMatcher = nameStartsWith(trimmed);
                            else typeMatcher = typeMatcher.or(nameStartsWith(trimmed));
                        }
                    }
                }

                if (typeMatcher == null) {
                    // safe default
                    typeMatcher = nameStartsWith("ir.myhome.spring.");
                }

                final ElementMatcher.Junction<?> finalTypeMatcher = typeMatcher;

                matcherBuilder = matcherBuilder
                        .type((ElementMatcher<? super TypeDescription>) finalTypeMatcher)
                        .transform((b, typeDescription, classLoader, module, protectionDomain) -> {
                            var commonMatcher = isMethod()
                                    .and(not(isConstructor()))
                                    .and(not(isAbstract()))
                                    .and(isPublic())
                                    .and(not(isStatic()));

                            // install enter+exit together to preserve @Advice.Enter propagation
                            b = b.visit(Advice.to(TimingAdviceEnter.class, TimingAdviceExitDynamic.class).on(commonMatcher));
                            return b;
                        });

                builder = matcherBuilder;
            }

            builder.installOn(inst);
            System.out.println("[InstrumentationInstaller] instrumentation installed");
        } catch (Throwable t) {
            System.err.println("[InstrumentationInstaller] install failed: " + t.getMessage());
            t.printStackTrace();
        }
    }
}

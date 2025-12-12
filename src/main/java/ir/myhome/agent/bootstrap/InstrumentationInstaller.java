package ir.myhome.agent.bootstrap;

import ir.myhome.agent.config.AgentConfig;
import ir.myhome.agent.config.AgentConfig.TimingConfig;
import ir.myhome.agent.instrumentation.advice.*;
import ir.myhome.agent.metrics.MetricCollectorSingleton;
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

        // MetricCollectorSingleton init
        MetricCollectorSingleton.init(cfg);

        AgentBuilder builder = new AgentBuilder.Default().with(listener).with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION).ignore(nameStartsWith("java.").or(nameStartsWith("javax.")).or(nameStartsWith("sun.")).or(nameStartsWith("jdk.")).or(nameStartsWith("org.apache.")).or(nameStartsWith("org.springframework.")).or(nameStartsWith("com.sun.")).or(nameStartsWith("net.bytebuddy.")));

        try {
            if (cfg.instrumentation.executor.enabled) {
                builder = builder.type(isSubTypeOf(java.util.concurrent.ExecutorService.class).and(nameStartsWith(cfg.rootPackage))).transform((b, td, cl, md, pd) -> b.method(named("execute").or(named("submit")).or(named("invokeAll")).or(named("invokeAny"))).intercept(Advice.to(ExecutorServiceAdvice.class)));
            }

            if (cfg.instrumentation.jdbc.enabled) {
                builder = builder.type(nameStartsWith(cfg.rootPackage).and(nameContains("jdbc").or(nameContains("dao")).or(nameContains("repository")))).transform((b, td, cl, md, pd) -> b.method(named("execute").or(named("executeQuery")).or(named("executeUpdate"))).intercept(Advice.to(JdbcAdvice.class)));
            }

            // ===== TIMING + Percentile =====
            if (cfg.instrumentation.timing != null && cfg.instrumentation.timing.enabled) {
                TimingConfig timing = cfg.instrumentation.timing;
                AgentBuilder matcherBuilder = builder;

                ElementMatcher.Junction<?> typeMatcher = null;
                List<String> entries = timing.entrypoints;

                if (entries == null || entries.isEmpty()) {
                    System.out.println("[InstrumentationInstaller] WARNING: timing.entrypoints empty, using default prefix");
                    typeMatcher = nameStartsWith(cfg.rootPackage);
                } else {
                    for (String ep : entries) {
                        if (ep == null || ep.isEmpty()) continue;

                        String trimmed = ep.trim();
                        if (trimmed.endsWith(".*")) trimmed = trimmed.substring(0, trimmed.length() - 2);

                        if (typeMatcher == null) typeMatcher = nameStartsWith(trimmed);
                        else typeMatcher = typeMatcher.or(nameStartsWith(trimmed));
                    }
                }

                if (typeMatcher == null) typeMatcher = nameStartsWith(cfg.rootPackage);

                final ElementMatcher.Junction<?> finalTypeMatcher = typeMatcher;

                builder = matcherBuilder.type((ElementMatcher<? super TypeDescription>) finalTypeMatcher).transform((b, typeDescription, classLoader, module, protectionDomain) -> {
                    var commonMatcher = isMethod().and(not(isConstructor())).and(not(isAbstract())).and(isPublic()).and(not(isStatic()));

                    // Advice برای Timing و Percentile
                    b = b.visit(Advice.to(TimingAdviceEnter.class, TimingAdviceExitDynamic.class).on(commonMatcher));
                    b = b.visit(Advice.to(PercentileAdvice.class).on(commonMatcher));

                    return b;
                });
            }

            builder.installOn(inst);
            System.out.println("[InstrumentationInstaller] instrumentation installed");

        } catch (Throwable t) {
            System.err.println("[InstrumentationInstaller] install failed: " + t.getMessage());
            t.printStackTrace();
        }
    }
}

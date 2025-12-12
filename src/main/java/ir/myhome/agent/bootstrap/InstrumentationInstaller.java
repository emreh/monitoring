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

    private InstrumentationInstaller() {
    }

    public static void install(Instrumentation inst, AgentConfig cfg) {
        AgentBuilder.Listener listener = new SimpleErrorListener(System.out);

        // قبل از نصب، MetricCollectorSingleton رو مقداردهی می‌کنیم
        try {
            MetricCollectorSingleton.init(cfg);
        } catch (Throwable t) {
            System.err.println("[InstrumentationInstaller] MetricCollectorSingleton.init failed: " + t.getMessage());
            t.printStackTrace();
        }

        AgentBuilder builder = new AgentBuilder.Default().with(listener).with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION).ignore(nameStartsWith("java.").or(nameStartsWith("javax.")).or(nameStartsWith("sun.")).or(nameStartsWith("jdk.")).or(nameStartsWith("org.apache.")).or(nameStartsWith("org.springframework.")).or(nameStartsWith("com.sun.")).or(nameStartsWith("net.bytebuddy.")));

        try {
            // Executor instrumentation (optional)
            if (cfg.instrumentation != null && cfg.instrumentation.executor != null && cfg.instrumentation.executor.enabled) {
                builder = builder.type(isSubTypeOf(java.util.concurrent.ExecutorService.class).and(nameStartsWith(cfg.rootPackage))).transform((b, td, cl, md, pd) -> b.method(named("execute").or(named("submit")).or(named("invokeAll")).or(named("invokeAny"))).intercept(Advice.to(ExecutorServiceAdvice.class)));
                System.out.println("[InstrumentationInstaller] ExecutorService instrumentation enabled");
            }

            // JDBC instrumentation (optional)
            if (cfg.instrumentation != null && cfg.instrumentation.jdbc != null && cfg.instrumentation.jdbc.enabled) {
                builder = builder.type(nameStartsWith(cfg.rootPackage).and(nameContains("jdbc").or(nameContains("dao")).or(nameContains("repository")))).transform((b, td, cl, md, pd) -> b.method(named("execute").or(named("executeQuery")).or(named("executeUpdate"))).intercept(Advice.to(JdbcAdvice.class)));
                System.out.println("[InstrumentationInstaller] JDBC instrumentation enabled");
            }

            // ===== TIMING + Percentile =====
            if (cfg.instrumentation != null && cfg.instrumentation.timing != null && cfg.instrumentation.timing.enabled) {
                TimingConfig timing = cfg.instrumentation.timing;
                AgentBuilder matcherBuilder = builder;

                net.bytebuddy.matcher.ElementMatcher.Junction<?> typeMatcher = null;
                List<String> entries = timing.entrypoints;

                if (entries == null || entries.isEmpty()) {
                    System.out.println("[InstrumentationInstaller] WARNING: timing.entrypoints is empty -> using default prefix " + cfg.rootPackage);
                    typeMatcher = nameStartsWith(cfg.rootPackage);
                } else {
                    for (String ep : entries) {
                        if (ep == null || ep.isEmpty()) continue;

                        String trimmed = ep.trim();

                        if (trimmed.endsWith(".*")) {
                            String prefix = trimmed.substring(0, trimmed.length() - 2);

                            if (typeMatcher == null) typeMatcher = nameStartsWith(prefix + ".");
                            else typeMatcher = typeMatcher.or(nameStartsWith(prefix + "."));
                        } else {
                            if (trimmed.endsWith(".")) trimmed = trimmed.substring(0, trimmed.length() - 1);

                            if (typeMatcher == null) typeMatcher = nameStartsWith(trimmed);
                            else typeMatcher = typeMatcher.or(nameStartsWith(trimmed));
                        }
                    }
                }

                if (typeMatcher == null) {
                    typeMatcher = nameStartsWith(cfg.rootPackage);
                }

                final ElementMatcher.Junction<?> finalTypeMatcher = typeMatcher;

                matcherBuilder = matcherBuilder.type((ElementMatcher<? super TypeDescription>) finalTypeMatcher).transform((b, typeDescription, classLoader, module, protectionDomain) -> {
                    var commonMatcher = isMethod().and(not(isConstructor())).and(not(isAbstract())).and(isPublic()).and(not(isStatic()));

                    // اضافه کردن Advice برای Timing (Enter + Exit dynamic)
                    b = b.visit(Advice.to(TimingAdviceEnter.class, TimingAdviceExitDynamic.class).on(commonMatcher));

                    // اضافه کردن PercentileAdvice برای هر متد
                    b = b.visit(Advice.to(PercentileAdvice.class).on(commonMatcher));

                    return b;
                });

                builder = matcherBuilder;
                System.out.println("[InstrumentationInstaller] Timing + Percentile instrumentation enabled for rootPackage=" + cfg.rootPackage);
            }

            builder.installOn(inst);
            System.out.println("[InstrumentationInstaller] instrumentation installed");

        } catch (Throwable t) {
            System.err.println("[InstrumentationInstaller] install failed: " + t.getMessage());
            t.printStackTrace();
        }
    }
}

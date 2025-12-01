package ir.myhome.agent.instrumentation;

import ir.myhome.agent.config.AgentConfig;
import ir.myhome.agent.instrumentation.advice.*;
import ir.myhome.agent.util.listener.SimpleErrorListener;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.*;

public final class InstrumentationInstaller {

    public static void install(Instrumentation inst, AgentConfig cfg) {
        AgentBuilder.Listener listener = new SimpleErrorListener(System.out);

        AgentBuilder base = new AgentBuilder.Default().with(listener).with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION).with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE).ignore(nameStartsWith("net.bytebuddy.")).ignore(nameStartsWith("sun.")).ignore(nameStartsWith("java."));

        try {
            if (cfg.instrumentationExecutor) {
                base = base.type(isSubTypeOf(java.util.concurrent.ExecutorService.class)).transform((b, td, cl, md, pd) -> b.method(any()).intercept(Advice.to(ExecutorServiceAdvice.class)));
            }

            if (cfg.instrumentationCompletable) {
                base = base.type(named("java.util.concurrent.CompletableFuture")).transform((b, td, cl, md, pd) -> b.method(named("supplyAsync").or(named("runAsync"))).intercept(Advice.to(CompletableFutureAdvice.class)));
            }

            if (cfg.instrumentationHttpClient) {
                base = base.type(named("java.net.http.HttpClient")).transform((b, td, cl, md, pd) -> b.method(named("send").or(named("sendAsync"))).intercept(Advice.to(HttpClientAdvice.class)));
            }

            if (cfg.instrumentationJdbc) {
                base = base.type(nameContains("java.sql")).transform((b, td, cl, md, pd) -> b.method(named("execute").or(named("executeQuery")).or(named("executeUpdate"))).intercept(Advice.to(JdbcAdvice.class)));
            }

            if (cfg.instrumentationScheduled) {
                base = base.type(isSubTypeOf(java.util.concurrent.ScheduledExecutorService.class)).transform((b, td, cl, md, pd) -> b.method(any()).intercept(Advice.to(ScheduledExecutorServiceAdvice.class)));
            }

            // timing advice for application package (optional — adjust package if needed)
            base = base.type(nameStartsWith("ir.myhome.spring").and(not(nameStartsWith("ir.myhome.agent")))).transform((b, td, cl, md, pd) -> b.visit(Advice.to(TimingAdvice.class).on(isMethod().and(not(isConstructor())).and(not(isAbstract())))));

            base.installOn(inst);
        } catch (Throwable t) {
            System.err.println("[InstrumentationInstaller] install failed: " + t.getMessage());
            t.printStackTrace();
        }
    }
}

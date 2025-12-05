package ir.myhome.agent.instrumentation;

import ir.myhome.agent.config.AgentConfig;
import ir.myhome.agent.instrumentation.advice.ExecutorServiceAdvice;
import ir.myhome.agent.instrumentation.advice.JdbcAdvice;
import ir.myhome.agent.instrumentation.advice.TimingAdviceEnter;
import ir.myhome.agent.instrumentation.advice.TimingAdviceExit;
import ir.myhome.agent.util.listener.SimpleErrorListener;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.*;

public final class InstrumentationInstaller {

    public static void install(Instrumentation inst, AgentConfig cfg) {
        AgentBuilder.Listener listener = new SimpleErrorListener(System.out);

        AgentBuilder builder = new AgentBuilder.Default().with(listener).with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION).ignore(nameStartsWith("java.").or(nameStartsWith("javax.")).or(nameStartsWith("sun.")).or(nameStartsWith("jdk.")).or(nameStartsWith("org.apache.")).or(nameStartsWith("org.springframework.")).or(nameStartsWith("com.sun.")).or(nameStartsWith("net.bytebuddy.")));

        try {
            if (cfg.instrumentation.executor) {
                builder = builder.type(isSubTypeOf(java.util.concurrent.ExecutorService.class).and(nameStartsWith("ir.myhome.spring."))).transform((b, td, cl, md, pd) -> b.method(named("execute").or(named("submit")).or(named("invokeAll")).or(named("invokeAny"))).intercept(Advice.to(ExecutorServiceAdvice.class)));
            }

            if (cfg.instrumentation.jdbc) {
                builder = builder.type(nameStartsWith("ir.myhome.spring.").and(nameContains("jdbc").or(nameContains("dao")).or(nameContains("repository")))).transform((b, td, cl, md, pd) -> b.method(named("execute").or(named("executeQuery")).or(named("executeUpdate"))).intercept(Advice.to(JdbcAdvice.class)));
            }

            if (cfg.instrumentation.timing) {
                builder = builder.type(nameStartsWith("ir.myhome.spring.")).transform((b, td, cl, md, pd) -> b.visit(Advice.to(TimingAdviceEnter.class).on(isMethod().and(not(isConstructor())).and(not(isAbstract()))))).transform((b, td, cl, md, pd) -> b.visit(Advice.to(TimingAdviceExit.class).on(isMethod().and(not(isConstructor())).and(not(isAbstract())))));
            }

            builder.installOn(inst);
            System.out.println("[InstrumentationInstaller] instrumentation installed");
        } catch (Throwable t) {
            System.err.println("[InstrumentationInstaller] install failed: " + t.getMessage());
            t.printStackTrace();
        }
    }
}

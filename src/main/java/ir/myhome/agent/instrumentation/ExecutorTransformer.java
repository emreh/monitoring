package ir.myhome.agent.instrumentation;

import ir.myhome.agent.instrumentation.advice.ExecutorTraceAdvice;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

import java.security.ProtectionDomain;

import static net.bytebuddy.matcher.ElementMatchers.*;

public final class ExecutorTransformer implements AgentBuilder.Transformer {

    @Override
    public DynamicType.Builder<?> transform(
            DynamicType.Builder<?> builder,
            TypeDescription typeDescription,
            ClassLoader classLoader,
            JavaModule module,
            ProtectionDomain protectionDomain) {

        return builder
                .method(named("execute")
                        .and(takesArguments(1))
                        .and(takesArgument(0, Runnable.class)))
                .intercept(net.bytebuddy.asm.Advice.to(ExecutorTraceAdvice.class))

                .method(named("submit")
                        .and(takesArguments(1))
                        .and(takesArgument(0, Runnable.class)))
                .intercept(net.bytebuddy.asm.Advice.to(ExecutorTraceAdvice.class));
    }
}

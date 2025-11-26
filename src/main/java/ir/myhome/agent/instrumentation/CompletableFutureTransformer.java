package ir.myhome.agent.instrumentation;

import ir.myhome.agent.instrumentation.advice.CompletableFutureAdvice;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.security.ProtectionDomain;

public final class CompletableFutureTransformer implements AgentBuilder.Transformer {

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain) {

        return builder
                .method(ElementMatchers.isStatic()
                        .and(ElementMatchers.takesArguments(1))
                        .and(ElementMatchers.nameStartsWith("runAsync")
                                .or(ElementMatchers.nameStartsWith("supplyAsync"))))
                .intercept(Advice.to(CompletableFutureAdvice.class));
    }
}

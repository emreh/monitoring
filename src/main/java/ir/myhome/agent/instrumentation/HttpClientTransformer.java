package ir.myhome.agent.instrumentation;

import ir.myhome.agent.instrumentation.advice.HttpClientAdvice;
import net.bytebuddy.agent.builder.AgentBuilder;

import static net.bytebuddy.matcher.ElementMatchers.named;

public final class HttpClientTransformer {

    public static AgentBuilder.Transformer transformer() {
        return (builder, td, cl, module, protectionDomain) -> builder.method(named("send").or(named("sendAsync"))).intercept(net.bytebuddy.asm.Advice.to(HttpClientAdvice.class));
    }
}

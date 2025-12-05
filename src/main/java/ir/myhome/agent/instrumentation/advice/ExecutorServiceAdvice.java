// file: src/main/java/ir/myhome/agent/instrumentation/advice/ExecutorServiceAdvice.java
package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.config.AgentConfig;
import ir.myhome.agent.context.TraceAwareCallable;
import ir.myhome.agent.context.TraceAwareRunnable;
import ir.myhome.agent.core.AgentContext;
import ir.myhome.agent.core.TraceContextHolder;
import ir.myhome.agent.core.TraceContextSnapshot;
import net.bytebuddy.asm.Advice;

import java.util.concurrent.Callable;

/**
 * این Advice ورودی‌های execute/submit/etc را wrap می‌کند — اما transform
 * تنها برای application classes (ir.myhome.*) فعال است تا Tomcat/JDK را نه بزنیم.
 */
public final class ExecutorServiceAdvice {

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void wrap(@Advice.AllArguments(readOnly = true) Object[] args, @Advice.AllArguments(readOnly = false) Object[] writableArgs) {
        try {
            if (writableArgs == null || writableArgs.length == 0) return;

            TraceContextSnapshot snap = TraceContextHolder.capture();

            for (int i = 0; i < writableArgs.length; i++) {
                Object a = writableArgs[i];

                if (a instanceof Runnable) {
                    writableArgs[i] = new TraceAwareRunnable((Runnable) a, snap);
                } else if (a instanceof Callable) {
                    writableArgs[i] = new TraceAwareCallable<>((Callable<?>) a, snap);
                }
            }
        } catch (Throwable t) {
            if (AgentContext.getAgentConfig().debug) {
                System.err.println("[ExecutorServiceAdvice] wrap failed: " + t);
            }
        }
    }
}

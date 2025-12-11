package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.metrics.MetricCollectorSingleton;
import net.bytebuddy.asm.Advice;

import java.lang.reflect.Method;

public final class PercentileAdvice {

    @Advice.OnMethodEnter
    public static long enter() {
        return System.currentTimeMillis();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void exit(@Advice.Origin Method method, @Advice.Enter long start, @Advice.Thrown Throwable throwable) {

        long duration = System.currentTimeMillis() - start;
        String metricName = method.getDeclaringClass().getSimpleName() + "." + method.getName();

        var collector = MetricCollectorSingleton.get();
        collector.recordLatency(metricName, duration);
        collector.incrementCount(metricName);
        if (throwable != null) collector.incrementError(metricName);

        // اضافه کردن snapshot به صف برای batch export
        collector.enqueueForExport(metricName);
    }
}

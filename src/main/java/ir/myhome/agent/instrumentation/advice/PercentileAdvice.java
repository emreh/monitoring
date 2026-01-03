package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.core.Span;
import ir.myhome.agent.core.TraceContextHolder;
import ir.myhome.agent.scheduler.PercentileBatchScheduler;
import net.bytebuddy.asm.Advice;

/**
 * Advice سبک: فقط زمان اجرای متد را می‌گیرد و به PercentileCollector می‌فرستد.
 * در InstrumentationInstaller از همین کلاس (PercentileAdvice) استفاده شده است.
 */
public final class PercentileAdvice {

    @Advice.OnMethodEnter
    public static long onEnter(@Advice.Origin String method) {
        return System.currentTimeMillis();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit(@Advice.Origin String method, @Advice.Enter long start, @Advice.Thrown Throwable throwable) {
        long duration = System.currentTimeMillis() - start;

        // ایجاد span
        Long traceId = TraceContextHolder.currentTraceId();
        Span span = new Span(traceId, traceId + "-" + System.nanoTime(), null, "service", method, start, duration);
        TraceContextHolder.pushSpan(span);

        // ثبت duration در PercentileOrchestrator از طریق PercentileBatchScheduler
        PercentileBatchScheduler.record(duration);
    }
}

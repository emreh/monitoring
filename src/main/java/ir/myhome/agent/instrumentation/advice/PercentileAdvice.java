package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.collector.PercentileCollector;
import net.bytebuddy.asm.Advice;

/**
 * Advice سبک: فقط زمان اجرای متد را می‌گیرد و به PercentileCollector می‌فرستد.
 * در InstrumentationInstaller از همین کلاس (PercentileAdvice) استفاده شده است.
 */
public final class PercentileAdvice {

    @Advice.OnMethodEnter
    public static long onEnter() {
        return System.currentTimeMillis();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit(@Advice.Origin String method, @Advice.Enter long start, @Advice.Thrown Throwable thrown) {
        long duration = Math.max(0, System.currentTimeMillis() - start);

        // method string از ByteBuddy معمولاً شامل نوع و سیگنچر. می‌توانیم آن را به عنوان endpoint استفاده کنیم.
        String endpoint = method == null ? "unknown" : method;

        // service را می‌توان از config یا از پکیج استخراج کرد؛ برای حالا root service ثابت:
        String service = "app"; // در صورت نیاز این مقدار را از AgentConfig بگیر

        // record only duration + endpoint
        PercentileCollector.record(service, endpoint, duration);
    }
}

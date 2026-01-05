package ir.myhome.agent.bootstrap;

import ir.myhome.agent.collector.SpanCollector;
import ir.myhome.agent.core.Aggregator;
import ir.myhome.agent.core.MetricsAggregator;
import ir.myhome.agent.core.Span;
import ir.myhome.agent.queue.SpanQueue;
import ir.myhome.agent.queue.SpanQueueImpl;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.concurrent.TimeUnit;

public class OptimizedInstrumentation {

    public static void installInstrumentation(String packageName) {
        try {
            // اینجا همه متدهای پکیج شما را اینسترومنت می‌کنیم
            new ByteBuddy().redefine(Object.class)  // کلاس هدف
                    .method(ElementMatchers.nameStartsWith(packageName))  // انتخاب پکیج خاص
                    .intercept(Advice.to(SpanAdvice.class))  // Advice برای اعمال اسپان
                    .make()
                    .load(OptimizedInstrumentation.class.getClassLoader());

            System.out.println("Instrumentation applied to target methods.");

        } catch (Exception e) {
            System.err.println("Error in ByteBuddy instrumentation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Advice برای ایجاد اسپان و ثبت زمان ورود و خروج
    public static class SpanAdvice {

        // نگهداری زمان ورود برای هر متد
        private static final ThreadLocal<Long> startTime = new ThreadLocal<>();

        @Advice.OnMethodEnter
        public static void interceptEnter(@Advice.This Object thiz) {
            // زمان ورود به متد
            startTime.set(System.nanoTime());  // زمان ورود با nanoTime ثبت می‌شود
            System.out.println("Entering method: " + thiz.getClass().getName());
        }

        @Advice.OnMethodExit
        public static void interceptExit(@Advice.This Object thiz) {
            // زمان خروج از متد
            long endTime = System.nanoTime();  // زمان خروج با nanoTime ثبت می‌شود

            // محاسبه زمان سپری شده
            long elapsedTime = endTime - startTime.get();  // تفاوت زمان ورود و خروج

            // تبدیل به میلی‌ثانیه برای نمایش و گزارش
            long elapsedTimeMs = TimeUnit.NANOSECONDS.toMillis(elapsedTime);

            // ثبت اسپان برای متد
            Span span = new Span(System.currentTimeMillis(), "span-" + thiz.getClass().getName(), null, "serviceName", thiz.getClass().getName(), System.currentTimeMillis());
            span.setDuration(elapsedTimeMs);  // اضافه کردن زمان سپری‌شده به اسپان
            span.end();  // پایان اسپان

            // ارسال اسپان به صف
            SpanQueue<Span> spanQueue = new SpanQueueImpl<>(10000);
            SpanCollector collector = new SpanCollector(spanQueue, new Aggregator(new MetricsAggregator()));
            collector.collect(span);  // ارسال اسپان به صف

            // گزارش زمان سپری شده
            System.out.println("Method execution time: " + elapsedTimeMs + " ms");
        }
    }
}

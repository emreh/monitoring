package ir.myhome.agent.util;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;

public class SystemLoadCalculator {

    private static final OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    // محاسبه بار سیستم و نرخ نمونه‌برداری
    public static int calculateSampleRate() {
        // دریافت بار CPU
        double systemLoad = osBean.getSystemLoadAverage();

        // در صورتی که بار سیستم قابل اندازه‌گیری نباشد (مثلاً سیستم بدون بار است)
        if (systemLoad == -1) {
            systemLoad = 0;  // اگر نتوانستیم بار سیستم را بدست آوریم، فرض می‌کنیم که بار صفر است.
        }

        // می‌توانیم از معیارهای مختلف استفاده کنیم. اینجا از درصد استفاده از CPU استفاده می‌کنیم.
        // اگر بار CPU بیشتر از 80 درصد باشد، نرخ نمونه‌برداری کاهش می‌یابد.
        // اگر بار پایین باشد، نرخ نمونه‌برداری افزایش می‌یابد.

        int sampleRate;
        if (systemLoad > 0.8) {
            sampleRate = 5;  // اگر بار سیستم بیشتر از 80 درصد باشد، نرخ نمونه‌برداری را کاهش می‌دهیم (هر 1 از 5 داده‌ها نمونه‌برداری می‌شود)
        } else if (systemLoad > 0.5) {
            sampleRate = 10;  // اگر بار سیستم بین 50 تا 80 درصد باشد، نرخ نمونه‌برداری به 1 از 10 می‌رسد
        } else {
            sampleRate = 20;  // اگر بار سیستم کمتر از 50 درصد باشد، نرخ نمونه‌برداری به 1 از 20 می‌رسد
        }

        return sampleRate;
    }
}


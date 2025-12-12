package ir.myhome.agent.util;

import java.util.Arrays;

public final class PercentileCalculator {

    private PercentileCalculator() {
    }

    /**
     * محاسبهٔ یک percentile ساده روی آرایهٔ مقادیر (ms).
     * p مقدار در بازهٔ 0..100 (مثلاً 50، 90، 99)
     * خروجی به صورت double (ms) برمی‌گردد.
     */
    public static double percentile(long[] values, double p) {
        if (values == null || values.length == 0) return 0.0;

        long[] copy = Arrays.copyOf(values, values.length);
        Arrays.sort(copy);
        double rank = p / 100.0 * (copy.length - 1);
        int low = (int) Math.floor(rank);
        int high = (int) Math.ceil(rank);

        if (low == high) return copy[low];

        double fraction = rank - low;
        return copy[low] + fraction * (copy[high] - copy[low]);
    }
}

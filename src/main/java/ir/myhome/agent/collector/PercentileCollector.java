package ir.myhome.agent.collector;

public interface PercentileCollector {

    // ثبت داده‌های جدید
    void record(long value);

    // محاسبه درصد
    double percentile(double p);

    // تعداد داده‌های جمع‌آوری‌شده
    long count();
}

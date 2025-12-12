package ir.myhome.agent.collector;

public interface PercentileCollector {

    void record(long value);

    double percentile(double p); // p بین 0 تا 100

    long count();
}

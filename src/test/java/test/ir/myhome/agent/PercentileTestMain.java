package test.ir.myhome.agent;

import ir.myhome.agent.config.AgentConfig;
import ir.myhome.agent.metrics.MetricCollector;
import ir.myhome.agent.metrics.MetricCollectorSingleton;

public class PercentileTestMain {

    public static void main(String[] args) throws Exception {
        // ساخت AgentConfig
        AgentConfig cfg = new AgentConfig();
        cfg.enableAdvancedMetrics = true;
        cfg.percentileMaxValueMs = 100; // حداکثر ثبت 100ms
        cfg.percentilePrecision = 2;

        // مقداردهی Singleton
        MetricCollectorSingleton.init(cfg);
        MetricCollector collector = MetricCollectorSingleton.get();

        SampleService service = new SampleService();

        // شبیه‌سازی فراخوانی متدها و ثبت latency
        for (int i = 0; i < 5; i++) {
            long start = System.currentTimeMillis();
            service.fastMethod();
            long duration = System.currentTimeMillis() - start;
            collector.recordLatency("SampleService.fastMethod", duration);
            collector.incrementCount("SampleService.fastMethod");
        }

        for (int i = 0; i < 5; i++) {
            long start = System.currentTimeMillis();
            service.slowMethod();
            long duration = System.currentTimeMillis() - start;
            collector.recordLatency("SampleService.slowMethod", duration);
            collector.incrementCount("SampleService.slowMethod");
        }

        // خواندن Percentile
        System.out.println("FastMethod P95: " + collector.getPercentile("SampleService.fastMethod", 95));
        System.out.println("SlowMethod P95: " + collector.getPercentile("SampleService.slowMethod", 95));
        System.out.println("FastMethod Count: " + collector.getCount("SampleService.fastMethod"));
        System.out.println("SlowMethod Count: " + collector.getCount("SampleService.slowMethod"));
    }
}

class SampleService {

    public void fastMethod() throws InterruptedException {
        Thread.sleep(50);
    }

    public void slowMethod() throws InterruptedException {
        Thread.sleep(120);
    }

    public void errorMethod() throws Exception {
        throw new RuntimeException("Test exception");
    }
}
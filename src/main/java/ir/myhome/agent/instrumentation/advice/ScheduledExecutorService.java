package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.exporter.SpanExporter;

import java.lang.instrument.Instrumentation;

public class ScheduledExecutorService {

    public static void install(Instrumentation inst, SpanExporter exporter) {
        // intercept schedule(), scheduleAtFixedRate(), scheduleWithFixedDelay()
    }
}

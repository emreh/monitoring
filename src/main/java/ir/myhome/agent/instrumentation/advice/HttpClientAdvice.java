package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.exporter.SpanExporter;

import java.lang.instrument.Instrumentation;

public final class HttpClientAdvice {

    public static void install(Instrumentation inst, SpanExporter exporter) {
        // before HttpClient.send → create span
        // after send → end span
    }
}


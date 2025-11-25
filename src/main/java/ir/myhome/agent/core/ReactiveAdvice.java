package ir.myhome.agent.core;

import ir.myhome.agent.exporter.SpanExporter;

import java.lang.instrument.Instrumentation;

public class ReactiveAdvice {

    public static void install(Instrumentation inst, SpanExporter exporter) {
        // wrap reactor.core.publisher.Mono & Flux → span around subscribe()
    }
}

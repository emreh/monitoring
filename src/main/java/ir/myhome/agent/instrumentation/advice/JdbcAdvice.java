package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.exporter.SpanExporter;

import java.lang.instrument.Instrumentation;

public final class JdbcAdvice {

    public static void install(Instrumentation inst, SpanExporter exporter) {
        // wrap executeQuery, executeUpdate, execute → SPAN
    }
}

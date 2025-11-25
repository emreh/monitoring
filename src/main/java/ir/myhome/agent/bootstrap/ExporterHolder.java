package ir.myhome.agent.bootstrap;

import ir.myhome.agent.exporter.SpanExporter;

public final class ExporterHolder {
    private static volatile SpanExporter exporter;

    private ExporterHolder() {
    }

    public static void setExporter(SpanExporter e) {
        exporter = e;
    }

    public static SpanExporter getExporter() {
        return exporter;
    }
}

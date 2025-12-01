package ir.myhome.agent.bootstrap;

import ir.myhome.agent.exporter.Exporter;

public final class ExporterHolder {

    private static volatile Exporter exporter;

    private ExporterHolder() {
    }

    public static void set(Exporter e) {
        exporter = e;
    }

    public static Exporter get() {
        return exporter;
    }
}

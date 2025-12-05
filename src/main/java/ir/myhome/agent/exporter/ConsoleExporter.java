package ir.myhome.agent.exporter;

import ir.myhome.agent.util.JsonSerializer;

import java.util.Map;

public final class ConsoleExporter implements Exporter {

    @Override
    public void export(Map<String, Object> span) {
        System.out.println("[ConsoleExporter] " + JsonSerializer.toJson(span));
    }

    @Override
    public void close() { /* no-op */ }
}

package ir.myhome.agent.exporter;

import ir.myhome.agent.core.Span;

import java.util.List;

public final class ConsoleExporter implements AgentExporter {

    @Override
    public void export(List<Span> batch) {
        for (Span span : batch) {
            System.out.println("[ConsoleExporter] " + span);
        }
    }
}

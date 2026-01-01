package ir.myhome.agent.exporter.impl;

import ir.myhome.agent.core.Span;
import ir.myhome.agent.exporter.AgentExporter;

import java.util.List;

public final class ConsoleExporter implements AgentExporter {

    @Override
    public void export(List<Span> batch) {
        for (Span span : batch) {
            System.out.println("[ConsoleExporter] " + span);
        }
    }
}

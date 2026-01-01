package ir.myhome.agent.exporter;

import ir.myhome.agent.core.Span;

import java.util.List;

public interface AgentExporter {
    void export(List<Span> batch);
}

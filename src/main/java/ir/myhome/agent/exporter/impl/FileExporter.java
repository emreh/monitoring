package ir.myhome.agent.exporter.impl;

import ir.myhome.agent.core.Span;
import ir.myhome.agent.exporter.AgentExporter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class FileExporter implements AgentExporter {
    private final String filePath = "agent-metrics.log";

    @Override
    public void export(List<Span> batch) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            for (Span span : batch) {
                writer.write(span.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("[FileExporter] Error: " + e.getMessage());
        }
    }
}
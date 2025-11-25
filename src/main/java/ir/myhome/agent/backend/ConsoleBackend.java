package ir.myhome.agent.backend;

import ir.myhome.agent.exporter.SpanExporterBackend;

import java.util.List;

public final class ConsoleBackend implements SpanExporterBackend {

    @Override
    public void exportBatch(List<String> jsonBatch) {
        if (jsonBatch == null || jsonBatch.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int i = 0; i < jsonBatch.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(jsonBatch.get(i));
        }

        sb.append("]");
        System.out.println("[ConsoleBackend] " + sb);
    }
}

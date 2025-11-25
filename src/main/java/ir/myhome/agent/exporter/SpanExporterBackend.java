package ir.myhome.agent.exporter;

import java.util.List;

public interface SpanExporterBackend {
    void exportBatch(List<String> jsonBatch) throws Exception;
}

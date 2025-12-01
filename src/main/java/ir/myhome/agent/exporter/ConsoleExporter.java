package ir.myhome.agent.exporter;

import java.util.List;

public final class ConsoleExporter implements Exporter {

    @Override
    public void export(List<String> jsonBatch) {
        if (jsonBatch == null || jsonBatch.isEmpty()) return;

        StringBuilder sb = new StringBuilder("[ConsoleExporter]");
        sb.append(jsonBatch.toString());
        System.out.println(sb.toString());
    }
}

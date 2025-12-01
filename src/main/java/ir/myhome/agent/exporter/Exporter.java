package ir.myhome.agent.exporter;

import java.util.List;

public interface Exporter {

    void export(List<String> jsonBatch);

    default void shutdown() {
    }
}

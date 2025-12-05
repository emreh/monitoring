package ir.myhome.agent.exporter;

import java.util.Map;

public interface Exporter {

    void export(Map<String, Object> span);

    void close();
}

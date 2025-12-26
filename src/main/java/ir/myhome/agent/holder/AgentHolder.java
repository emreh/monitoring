package ir.myhome.agent.holder;

import ir.myhome.agent.exporter.AgentExporter;
import ir.myhome.agent.queue.SpanQueue;

public final class AgentHolder {

    private static volatile AgentExporter exporter;
    private static volatile SpanQueue<?> spanQueue;

    private AgentHolder() {}

    public static void setExporter(AgentExporter exp) {
        exporter = exp;
    }

    public static AgentExporter getExporter() {
        return exporter;
    }

    public static void setSpanQueue(SpanQueue<?> queue) {
        spanQueue = queue;
    }

    public static SpanQueue<?> getSpanQueue() {
        return spanQueue;
    }
}

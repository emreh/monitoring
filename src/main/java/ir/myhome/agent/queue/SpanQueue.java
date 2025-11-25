package ir.myhome.agent.queue;

import ir.myhome.agent.core.Span;

public interface SpanQueue {

    boolean offer(Span span);

    int drainTo(Span[] buffer, int maxItems);

    int size();
}

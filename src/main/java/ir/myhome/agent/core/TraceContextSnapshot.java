package ir.myhome.agent.core;

public final class TraceContextSnapshot {

    TraceContextSnapshot parent;

    public Span currentSpan;

    public TraceContextSnapshot() {
        this.currentSpan = new Span();
    }
}

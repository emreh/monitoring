package ir.myhome.agent.core;

public final class TraceContextSnapshot {

    public final String traceId;
    public final String[] spanStack;
    public static final TraceContextSnapshot EMPTY = new TraceContextSnapshot(null, new String[0]);

    public TraceContextSnapshot(String traceId, String[] spanStack) {
        this.traceId = traceId;
        this.spanStack = spanStack == null ? new String[0] : spanStack;
    }
}

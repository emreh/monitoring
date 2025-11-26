package ir.myhome.agent.core;

public final class TraceContextHolder {
    private static final ThreadLocal<TraceContextSnapshot> current = new ThreadLocal<>();

    private TraceContextHolder() {}

    public static TraceContextSnapshot capture() {
        TraceContextSnapshot snap = new TraceContextSnapshot();
        snap.parent = current.get();
        return snap;
    }

    public static TraceContextSnapshot restore(TraceContextSnapshot snap) {
        TraceContextSnapshot prev = current.get();
        current.set(snap);
        return prev;
    }

    public static TraceContextSnapshot current() {
        return current.get();
    }
}

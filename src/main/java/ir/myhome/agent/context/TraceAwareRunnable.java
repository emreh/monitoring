package ir.myhome.agent.context;

import ir.myhome.agent.core.TraceContextHolder;
import ir.myhome.agent.core.TraceContextSnapshot;

public final class TraceAwareRunnable implements Runnable {
    private final Runnable delegate;
    private final TraceContextSnapshot snap;

    public TraceAwareRunnable(Runnable delegate, TraceContextSnapshot snap) {
        this.delegate = delegate;
        this.snap = snap == null ? TraceContextSnapshot.EMPTY : snap;
    }

    @Override
    public void run() {
        TraceContextSnapshot prev = TraceContextHolder.restore(snap);

        try {
            delegate.run();
        } finally {
            TraceContextHolder.restore(prev);
        }
    }
}

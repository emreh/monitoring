package ir.myhome.agent.context;

import ir.myhome.agent.core.TraceContextHolder;
import ir.myhome.agent.core.TraceContextSnapshot;

public final class TraceAwareRunnable implements Runnable {

    private final Runnable delegate;
    private final TraceContextSnapshot snapshot;

    public TraceAwareRunnable(Runnable delegate, TraceContextSnapshot snapshot) {
        this.delegate = delegate;
        this.snapshot = snapshot;
    }

    @Override
    public void run() {
        TraceContextSnapshot prev = TraceContextHolder.restore(snapshot);

        try {
            delegate.run();
        } finally {
            TraceContextHolder.restore(prev);
        }
    }
}

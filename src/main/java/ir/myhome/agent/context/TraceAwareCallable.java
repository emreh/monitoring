package ir.myhome.agent.context;

import ir.myhome.agent.core.TraceContextHolder;
import ir.myhome.agent.core.TraceContextSnapshot;

import java.util.concurrent.Callable;

public final class TraceAwareCallable<V> implements Callable<V> {

    private final Callable<V> delegate;
    private final TraceContextSnapshot snap;

    public TraceAwareCallable(Callable<V> delegate, TraceContextSnapshot snap) {
        this.delegate = delegate;
        this.snap = snap == null ? TraceContextSnapshot.EMPTY : snap;
    }

    @Override
    public V call() throws Exception {
        TraceContextSnapshot prev = TraceContextHolder.restore(snap);

        try {
            return delegate.call();
        } finally {
            TraceContextHolder.restore(prev);
        }
    }
}

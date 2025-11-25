package ir.myhome.agent.context;

import ir.myhome.agent.core.TraceContextHolder;
import ir.myhome.agent.core.TraceContextSnapshot;

import java.util.concurrent.Callable;

public final class TraceAwareCallable<V> implements Callable<V> {

    private final Callable<V> delegate;
    private final TraceContextSnapshot snapshot;

    public TraceAwareCallable(Callable<V> delegate, TraceContextSnapshot snapshot) {
        this.delegate = delegate;
        this.snapshot = snapshot == null ? TraceContextSnapshot.EMPTY : snapshot;
    }

    @Override
    public V call() throws Exception {
        TraceContextSnapshot prev = TraceContextHolder.restore(snapshot);
        try {
            return delegate.call();
        } finally {
            TraceContextHolder.restore(prev);
        }
    }
}

package ir.myhome.agent.context;

import ir.myhome.agent.core.TraceContextHolder;
import ir.myhome.agent.core.TraceContextSnapshot;

import java.util.function.Supplier;

public final class TraceAwareSupplier<T> implements Supplier<T> {

    private final Supplier<T> delegate;
    private final TraceContextSnapshot snap;

    public TraceAwareSupplier(Supplier<T> delegate, TraceContextSnapshot snap) {
        this.delegate = delegate;
        this.snap = snap == null ? TraceContextSnapshot.EMPTY : snap;
    }

    @Override
    public T get() {
        TraceContextSnapshot prev = TraceContextHolder.restore(snap);

        try {
            return delegate.get();
        } finally {
            TraceContextHolder.restore(prev);
        }
    }
}

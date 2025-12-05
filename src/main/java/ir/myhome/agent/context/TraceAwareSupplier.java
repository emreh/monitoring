package ir.myhome.agent.context;

import ir.myhome.agent.core.TraceContextHolder;
import ir.myhome.agent.core.TraceContextSnapshot;

import java.util.function.Supplier;

public final class TraceAwareSupplier<T> implements Supplier<T> {

    private final Supplier<T> delegate;
    private final TraceContextSnapshot snapshot;

    public TraceAwareSupplier(Supplier<T> delegate, TraceContextSnapshot snapshot) {
        this.delegate = delegate;
        this.snapshot = snapshot;
    }

    @Override
    public T get() {
        TraceContextSnapshot prev = TraceContextHolder.restore(snapshot);

        try {
            return delegate.get();
        } finally {
            TraceContextHolder.restore(prev);
        }
    }
}

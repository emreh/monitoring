package ir.myhome.agent;

import java.util.concurrent.Callable;

public class TraceableCallable<V> implements Callable<V> {

    private final Callable<V> delegate;
    private final String capturedTraceId;

    public TraceableCallable(Callable<V> delegate, String traceId) {
        this.delegate = delegate;
        this.capturedTraceId = traceId;
    }

    @Override
    public V call() throws Exception {
        if (capturedTraceId != null) {
            TraceContext.setTraceId(capturedTraceId);
        }

        try {
            return delegate.call();
        } finally {
            TraceContext.clear();
        }
    }
}

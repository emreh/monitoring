package ir.myhome.agent;

/**
 * این کلاس دقیقاً مثل یک «پاکت پستی» است؛ نامه (runnable اصلی) را همراه با traceId از Thread اصلی برمی‌دارد و در Thread جدید باز می‌کند.
 */
public class TraceableRunnable implements Runnable {

    private final Runnable delegate;      // همان runnable اصلی
    private final String capturedTraceId; // traceId که از Thread اصلی گرفتیم

    public TraceableRunnable(Runnable delegate, String traceId) {
        this.delegate = delegate;
        this.capturedTraceId = traceId;
    }

    @Override
    public void run() {
        // قبل از اجرای کار اصلی، context را ست می‌کنیم
        if (capturedTraceId != null) {
            TraceContext.setTraceId(capturedTraceId);
        }

        try {
            delegate.run();  // کار اصلی
        } finally {
            // پاکسازی تا این traceId در threadpool باقی نماند
            TraceContext.clear();
        }
    }
}

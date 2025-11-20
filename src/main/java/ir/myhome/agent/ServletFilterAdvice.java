package ir.myhome.agent;

import net.bytebuddy.asm.Advice;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Instruments Servlet Filter doFilter(...) methods.
 * Enter: try read header "X-Trace-Id" via reflection from ServletRequest (if present).
 * Exit: clear ThreadLocal.
 */
public class ServletFilterAdvice {

    @Advice.OnMethodEnter
    public static void onEnter(@Advice.AllArguments Object[] args) {
        try {
            if (args != null && args.length > 0 && args[0] != null) {
                Object req = args[0]; // expected ServletRequest / HttpServletRequest
                // try to call getHeader("X-Trace-Id") via reflection (safe)
                Method getHeader = null;
                try {
                    getHeader = req.getClass().getMethod("getHeader", String.class);
                } catch (NoSuchMethodException nsme) {
                    // not HttpServletRequest; maybe ServletRequest -> try attribute "X-Trace-Id"
                }
                String traceId = null;
                if (getHeader != null) {
                    try {
                        Object val = getHeader.invoke(req, "X-Trace-Id");
                        if (val != null) traceId = Objects.toString(val, null);
                    } catch (Throwable ignored) {
                        // ignore reflection failures
                    }
                }
                // fallback: try getAttribute("X-Trace-Id")
                if (traceId == null) {
                    try {
                        Method getAttr = req.getClass().getMethod("getAttribute", String.class);
                        Object a = getAttr.invoke(req, "X-Trace-Id");
                        if (a != null) traceId = Objects.toString(a, null);
                    } catch (Throwable ignored) {
                    }
                }

                if (traceId == null || traceId.isEmpty()) {
                    traceId = java.util.UUID.randomUUID().toString();
                }
                TraceContext.set(traceId);
            } else {
                // no args or no request object: ensure there is a trace id for this thread
                TraceContext.getOrCreate();
            }
        } catch (Throwable t) {
            // swallow — do not impact application
        }
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit(@Advice.AllArguments Object[] args) {
        // clear thread local to avoid leaks
        try {
            TraceContext.clear();
        } catch (Throwable ignored) {
        }
    }
}

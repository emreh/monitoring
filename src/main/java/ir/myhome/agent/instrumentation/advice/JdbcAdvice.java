package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.bootstrap.AgentHolder;
import ir.myhome.agent.core.JsonSerializer;
import ir.myhome.agent.core.Span;
import ir.myhome.agent.core.TraceContext;
import ir.myhome.agent.core.TraceContextHolder;
import net.bytebuddy.asm.Advice;

import java.sql.Statement;

public final class JdbcAdvice {

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static Span enter(@Advice.Argument(0) Statement stmt) {
        try {
            String traceId = TraceContextHolder.currentTraceId();
            String spanId = TraceContext.newId();
            String parent = TraceContextHolder.currentSpanId();
            TraceContextHolder.pushSpan(spanId, traceId);
            return new Span(traceId, spanId, parent, "jdbc", stmt == null ? "stmt" : stmt.toString(), System.currentTimeMillis());
        } catch (Throwable t) {
            return null;
        }
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void exit(@Advice.Enter Span span, @Advice.Thrown Throwable thrown) {
        if (span == null) return;

        if (thrown != null) span.markError(thrown.getMessage());

        span.end();
        TraceContextHolder.popSpan();

        var q = AgentHolder.getSpanQueue();

        if (q != null) q.offer(span);
        else System.out.println("[JdbcAdvice] span: " + JsonSerializer.toJson(span));
    }
}

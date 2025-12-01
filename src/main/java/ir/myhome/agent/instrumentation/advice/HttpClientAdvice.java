package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.bootstrap.AgentHolder;
import ir.myhome.agent.core.JsonSerializer;
import ir.myhome.agent.core.Span;
import ir.myhome.agent.core.TraceContext;
import ir.myhome.agent.core.TraceContextHolder;
import net.bytebuddy.asm.Advice;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class HttpClientAdvice {

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static Span enter(@Advice.Argument(0) HttpRequest request) {
        try {
            String traceId = TraceContextHolder.currentTraceId();
            String spanId = TraceContext.newId();
            String parent = TraceContextHolder.currentSpanId();
            TraceContextHolder.pushSpan(spanId, traceId);

            Span s = new Span(traceId, spanId, parent, "http-client", request == null ? "unknown" : request.uri().getPath(), System.currentTimeMillis());
            if (request != null) {
                s.addTag("http.method", request.method());
                s.addTag("http.url", request.uri().toString());
            }
            return s;
        } catch (Throwable t) {
            return null;
        }
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void exit(@Advice.Enter Span span, @Advice.Thrown Throwable thrown, @Advice.Return(readOnly = false) HttpResponse<?> response) {
        if (span == null) return;

        try {
            if (thrown != null) span.markError(thrown.getMessage());
            else if (response != null) span.setStatusCode(response.statusCode());
        } catch (Throwable ignore) {
        }

        try {
            span.end();
        } catch (Throwable ignore) {
        }

        try {
            TraceContextHolder.popSpan();
        } catch (Throwable ignore) {
        }

        try {
            var q = AgentHolder.getSpanQueue();
            if (q != null) q.offer(span);
            else System.out.println("[HttpClientAdvice] span: " + JsonSerializer.toJson(span));
        } catch (Throwable t) {
            System.err.println("[HttpClientAdvice] publish failed: " + t.getMessage());
        }
    }
}

package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.core.Span;
import ir.myhome.agent.core.TraceContextHolder;
import ir.myhome.agent.holder.AgentHolder;
import ir.myhome.agent.util.JsonSerializer;
import ir.myhome.agent.util.SpanIdGenerator;
import net.bytebuddy.asm.Advice;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class HttpClientAdvice {

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void enter(@Advice.Argument(0) HttpRequest request, @Advice.Local("span") Span span) {
        try {
            String traceId = TraceContextHolder.currentTraceId();
            String spanId = SpanIdGenerator.nextId();
            String parent = TraceContextHolder.currentSpanId();

            span = new Span(traceId, spanId, parent, "http-client", request == null ? "unknown" : request.uri().getPath(), System.currentTimeMillis());
            TraceContextHolder.pushSpan(span);

            if (request != null) {
                span.addTag("http.method", request.method());
                span.addTag("http.url", request.uri().toString());
            }

        } catch (Throwable t) {
            // ignore
        }
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void exit(@Advice.Local("span") Span span, @Advice.Thrown Throwable thrown, @Advice.Return(readOnly = false) HttpResponse<?> response) {

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

package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.config.AgentConfig;
import ir.myhome.agent.core.AgentContext;
import ir.myhome.agent.core.Span;
import ir.myhome.agent.core.TraceContext;
import ir.myhome.agent.core.TraceContextHolder;
import ir.myhome.agent.holder.AgentHolder;
import ir.myhome.agent.util.JsonSerializer;
import net.bytebuddy.asm.Advice;
import tools.jackson.databind.ObjectMapper;

public final class TimingAdvice {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void enter(@Advice.Origin("#t.#m") String signature, @Advice.AllArguments Object[] args, @Advice.Local("spanRef") Span[] spanRef) {
        try {
            String traceId = TraceContextHolder.currentTraceId();
            String spanId = TraceContext.newId();
            String parent = TraceContextHolder.currentSpanId();
            TraceContextHolder.pushSpan(spanId, traceId);

            Span s = new Span(traceId, spanId, parent, extractService(signature), signature, System.currentTimeMillis());
            spanRef[0] = s;

            // log input arguments as JSON
            try {
                String argsJson = mapper.writeValueAsString(args);
                if (AgentContext.getAgentConfig().debug) {
                    System.out.println("[TimingAdvice] enter " + signature + " spanId=" + spanId + " args=" + argsJson);
                }
            } catch (Exception e) {
                if (AgentContext.getAgentConfig().debug) {
                    System.out.println("[TimingAdvice] enter " + signature + " spanId=" + spanId + " args=<unserializable>");
                }
            }
        } catch (Throwable t) {
            System.err.println("[TimingAdvice] enter failed: " + t.getMessage());
        }
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void exit(@Advice.Thrown Throwable thrown, @Advice.Return Object ret, @Advice.Local("spanRef") Span[] spanRef) {
        if (spanRef == null || spanRef.length == 0) return;

        Span span = spanRef[0];
        if (span == null) return;

        try {
            if (thrown != null) span.markError(thrown.getMessage());
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

        // log output value as JSON
        try {
            String retJson = ret != null ? mapper.writeValueAsString(ret) : "void";
            System.out.println("[TimingAdvice] exit " + span.endpoint + " return=" + retJson + " throwable=" + (thrown != null ? thrown.getClass().getSimpleName() : "null") + " durationMs=" + span.durationMs);
        } catch (Exception e) {
            System.out.println("[TimingAdvice] exit " + span.endpoint + " return=<unserializable> throwable=" + (thrown != null ? thrown.getClass().getSimpleName() : "null") + " durationMs=" + span.durationMs);
        }

        // publish to queue
        try {
            var q = AgentHolder.getSpanQueue();

            if (q != null) q.offer(span);
            else {
                // fallback: print
                System.out.println("[TimingAdvice] fallback span: " + JsonSerializer.toJson(span));
            }
        } catch (Throwable t) {
            System.err.println("[TimingAdvice] publish failed: " + t.getMessage());
        }
    }

    private static String extractService(String sig) {
        if (sig == null) return "unknown";

        int idx = sig.indexOf('.');
        return idx > 0 ? sig.substring(0, idx) : sig;
    }
}

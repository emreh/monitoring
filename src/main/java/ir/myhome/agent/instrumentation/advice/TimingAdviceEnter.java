package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.config.AgentContext;
import ir.myhome.agent.core.Span;
import ir.myhome.agent.core.TraceContextHolder;
import ir.myhome.agent.util.PrettyArgRenderer;
import ir.myhome.agent.util.SpanIdGenerator;
import net.bytebuddy.asm.Advice;

import java.util.StringJoiner;

public final class TimingAdviceEnter {

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static Span enter(@Advice.Origin("#t.#m") String signature, @Advice.AllArguments Object[] args) {
        try {
            String traceId = TraceContextHolder.currentTraceId();
            if (traceId == null) traceId = SpanIdGenerator.nextId(); // شروع یک trace جدید اگر لازم باشد
            String spanId = SpanIdGenerator.nextId();
            String parentId = TraceContextHolder.currentSpan() != null ? TraceContextHolder.currentSpan().spanId : null;

            Span span = new Span(traceId, spanId, parentId, extractService(signature), signature, System.currentTimeMillis());

            // ثبت آرگومان‌ها
            if (args != null && args.length > 0) {
                StringJoiner sj = new StringJoiner(", ", "[", "]");
                for (Object a : args) sj.add(PrettyArgRenderer.render(a));
                span.addTag("args", sj.toString());
            }

            TraceContextHolder.pushSpan(span);

            if (AgentContext.getAgentConfig().debug)
                System.out.println("[TimingAdviceEnter] enter " + signature + " spanId=" + spanId);

            return span;

        } catch (Throwable t) {
            if (AgentContext.getAgentConfig().debug) System.err.println("[TimingAdviceEnter] failed: " + t);
            return null;
        }
    }

    public static String extractService(String sig) {
        if (sig == null) return "unknown";

        int idx = sig.indexOf('.');
        return idx > 0 ? sig.substring(0, idx) : sig;
    }
}

package ir.myhome.agent.instrumentation.advice;

import ir.myhome.agent.config.AgentConfig;
import ir.myhome.agent.core.AgentContext;
import ir.myhome.agent.core.Span;
import ir.myhome.agent.core.TraceContextHolder;
import ir.myhome.agent.util.PrettyArgRenderer;
import ir.myhome.agent.util.SpanIdGenerator;
import net.bytebuddy.asm.Advice;

import java.util.StringJoiner;

/**
  اصلاح: مشکل NPE چون @Advice.Local Span[] ابتدا null است.
  بنابراین ابتدا آرایه را مقداردهی می‌کنیم و سپس spanRef[0] = s;
  Advice باید خیلی سبک بماند (بدون UUID, ObjectMapper, SecureRandom و...).
*/
public final class TimingAdviceEnter {

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void enter(@Advice.Origin("#t.#m") String signature, @Advice.AllArguments(readOnly = true) Object[] args, @Advice.Local("spanRef") Span[] spanRef) {
        try {
            // initialize the local array so we can safely write to spanRef[0]
            spanRef = new Span[1];

            String traceId = TraceContextHolder.currentTraceId();
            String spanId = SpanIdGenerator.nextId();
            String parent = TraceContextHolder.currentSpanId();
            TraceContextHolder.pushSpan(spanId, traceId);

            Span s = new Span(traceId, spanId, parent, extractService(signature), signature, System.currentTimeMillis());
            spanRef[0] = s;

            if (args != null && args.length > 0) {
                StringJoiner sj = new StringJoiner(", ", "[", "]");
                for (Object a : args) sj.add(PrettyArgRenderer.render(a));
                s.addTag("args", sj.toString());
            }

            if (AgentContext.getAgentConfig().debug) {
                System.out.println("[TimingAdviceEnter] enter " + signature + " spanId=" + spanId);
            }
        } catch (Throwable t) {
            // لاگ خطا برای دیباگ؛ در prod این را خاموش کن
            if (AgentContext.getAgentConfig().debug) {
                System.err.println("[TimingAdviceEnter] failed: " + t);
            }
        }
    }

    public static String extractService(String sig) {
        if (sig == null) return "unknown";

        int idx = sig.indexOf('.');
        return idx > 0 ? sig.substring(0, idx) : sig;
    }
}

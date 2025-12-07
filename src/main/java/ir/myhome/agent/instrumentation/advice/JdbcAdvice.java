package ir.myhome.agent.instrumentation.advice;

import net.bytebuddy.asm.Advice;

/*
  جایگاه jdbc advice؛ سبک نگه داشته شده — اگر بخواهی می‌توانم مچِرهای هدف را در Installer اضافه کنم.
  (در این نسخه اولیه ما فقط advice را آماده داریم.)
*/
public final class JdbcAdvice {

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void enter() {
        // minimal
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void exit() {
        // minimal
    }
}

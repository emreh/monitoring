package ir.myhome.agent;

import net.bytebuddy.asm.Advice;

public class CalcAdvice {

    @Advice.OnMethodEnter
    public static long onEnter(@Advice.Argument(0) int a, @Advice.Argument(1) int b) {
        long start = System.nanoTime();
        System.out.println("[Agent] Enter add(" + a + ", " + b + ")");
        return start;
    }

    @Advice.OnMethodExit
    public static void onExit(@Advice.Enter long start, @Advice.Return int result) {
        long duration = System.nanoTime() - start;
        System.out.println("[Agent] Exit add(), result=" + result + ", took " + duration + "ns");
    }
}

package ir.myhome.agent.util.listener;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.utility.JavaModule;

import java.io.PrintStream;

public class SimpleErrorListener extends AgentBuilder.Listener.Adapter {

    private final PrintStream out;

    public SimpleErrorListener(PrintStream out) {
        this.out = out;
    }

    @Override
    public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded, Throwable throwable) {
        out.println("[Agent-Error] type=" + typeName + " : " + throwable.getMessage());
        throwable.printStackTrace(out);
    }
}

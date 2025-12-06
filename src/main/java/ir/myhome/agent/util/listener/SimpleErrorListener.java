package ir.myhome.agent.util.listener;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

import java.io.PrintStream;

public final class SimpleErrorListener implements AgentBuilder.Listener {
    private final PrintStream out;

    public SimpleErrorListener(PrintStream out) {
        this.out = out;
    }

    @Override
    public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded, DynamicType dynamicType) {
        // no-op
    }

    @Override
    public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded, Throwable throwable) {
        out.println("========== BYTEBUDDY ERROR ==========");
        out.println("Type: " + typeName);
        out.println("Message: " + throwable.getMessage());
        out.println("Cause: " + throwable.getClass().getName());
        throwable.printStackTrace(out);
        out.println("======================================");
    }

    @Override
    public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
    }

    @Override
    public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
    }

    @Override
    public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded) {
    }
}

package ir.myhome.agent.util;

import ir.myhome.agent.exporter.Exporter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class StartupValidator {

    private StartupValidator() {
    }

    public static boolean validate(List<Exporter> exporters, long timeoutMs) {
        if (exporters == null || exporters.isEmpty()) return true;
        ExecutorService ex = Executors.newFixedThreadPool(Math.min(2, exporters.size()));

        try {
            for (Exporter e : exporters) {
                // if Exporter had a probe interface we would call it here
            }

            return true;
        } finally {
            ex.shutdownNow();
            try {
                ex.awaitTermination(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) {
            }
        }
    }
}

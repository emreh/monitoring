package ir.myhome.agent.config;

import java.util.Arrays;
import java.util.List;

public final class AgentConfig {

    public String rootPackage = "ir.myhome.spring.";

    public boolean debug = false;
    public ExporterConfig exporter = new ExporterConfig();
    public InstrumentationConfig instrumentation = new InstrumentationConfig();

    // new for phase 6
    public int queueCapacity = 10000;
    public int workerCount = 1;
    public int pollMillis = 20;

    public boolean enableAdvancedMetrics = true;
    public int percentileMaxValueMs = 60000; // برای histogram
    public int percentilePrecision = 2; // digits HDRHistogram

    public static final class ExporterConfig {
        public String type = "console";
        public int batchSize = 10;
        public int capacity = 10000;
        public String endpoint = "http://localhost:8080/collect";
    }

    public static final class InstrumentationConfig {
        public Feature executor = new Feature(true);
        public Feature jdbc = new Feature(false);
        public Feature httpClient = new Feature(false);
        public Feature scheduled = new Feature(false);

        public TimingConfig timing = new TimingConfig();

        public static final class Feature {
            public boolean enabled;

            public Feature() {
            }

            public Feature(boolean enabled) {
                this.enabled = enabled;
            }
        }
    }

    public static final class TimingConfig {
        public boolean enabled = true;
        /**
         * List of entrypoint patterns; supports suffix '*' for prefix matching.
         */
        public List<String> entrypoints = Arrays.asList("ir.myhome.spring.controller.*", "ir.myhome.spring.service.*");
    }

    @Override
    public String toString() {
        return "AgentConfig{" + "debug=" + debug + ", exporter=" + exporter + ", instrumentation=" + instrumentation + ", queueCapacity=" + queueCapacity + ", workerCount=" + workerCount + ", pollMillis=" + pollMillis + '}';
    }
}

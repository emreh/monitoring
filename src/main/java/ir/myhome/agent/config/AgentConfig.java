package ir.myhome.agent.config;

public final class AgentConfig {
    public boolean debug = false;
    public ExporterConfig exporter = new ExporterConfig();
    public InstrumentationConfig instrumentation = new InstrumentationConfig();

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
            public Feature() {}
            public Feature(boolean enabled) { this.enabled = enabled; }
        }
    }

    public static final class TimingConfig {
        public boolean enabled = true;
        /**
         * List of entrypoint patterns; supports suffix '*' for prefix matching.
         * Examples:
         *   - "ir.myhome.spring.controller.*"
         *   - "ir.myhome.spring.service.*"
         *
         * If null or empty -> fallback behavior (instrument ir.myhome.spring.*)
         */
        public java.util.List<String> entrypoints = java.util.Arrays.asList(
                "ir.myhome.spring.controller.*",
                "ir.myhome.spring.service.*"
        );
    }

    @Override
    public String toString() {
        return "AgentConfig{" +
                "debug=" + debug +
                ", exporter=" + exporter +
                ", instrumentation=" + instrumentation +
                '}';
    }
}

package ir.myhome.agent.config;

public final class AgentConfig {

    public boolean debug = true;

    public ExporterConfig exporter = new ExporterConfig();
    public InstrumentationConfig instrumentation = new InstrumentationConfig();

    public static class ExporterConfig {
        public String type = "console"; // console | http
        public int batchSize = 10;
        public int capacity = 10000;
        public String endpoint = "http://localhost:8080/collect";
    }

    public static class InstrumentationConfig {
        public boolean executor = true;
        public boolean jdbc = false;
        public boolean httpClient = false;
        public boolean scheduled = false;
        public boolean timing = true;
    }
}

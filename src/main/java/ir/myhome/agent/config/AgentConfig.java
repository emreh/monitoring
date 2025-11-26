package ir.myhome.agent.config;

public final class AgentConfig {
    // exporter
    public String exporterType = "console"; // console | http | none
    public int exporterBatchSize = 10;
    public int exporterCapacity = 10000;
    public String exporterEndpoint = "http://localhost:8080/collect";

    // instrumentation toggles (default safe)
    public boolean instrumentationApp = true;      // instrument app package methods (timing)
    public boolean instrumentationExecutor = true; // instrument executors inside app
    public boolean instrumentationHttpClient = false;
    public boolean instrumentationJdbc = false;
    public boolean instrumentationScheduled = false;
    public boolean instrumentationReactive = false;

    // boolean getters (used by installer)
    public boolean isInstrumentationApp() {
        return instrumentationApp;
    }

    public boolean isInstrumentationExecutor() {
        return instrumentationExecutor;
    }

    public boolean isInstrumentationHttpClient() {
        return instrumentationHttpClient;
    }

    public boolean isInstrumentationJdbc() {
        return instrumentationJdbc;
    }

    public boolean isInstrumentationScheduled() {
        return instrumentationScheduled;
    }

    public boolean isInstrumentationReactive() {
        return instrumentationReactive;
    }

    @Override
    public String toString() {
        return "AgentConfig{" +
                "exporterType='" + exporterType + '\'' +
                ", exporterBatchSize=" + exporterBatchSize +
                ", exporterCapacity=" + exporterCapacity +
                ", exporterEndpoint='" + exporterEndpoint + '\'' +
                ", instrumentationApp=" + instrumentationApp +
                ", instrumentationExecutor=" + instrumentationExecutor +
                ", instrumentationHttpClient=" + instrumentationHttpClient +
                ", instrumentationJdbc=" + instrumentationJdbc +
                ", instrumentationScheduled=" + instrumentationScheduled +
                ", instrumentationReactive=" + instrumentationReactive +
                '}';
    }
}

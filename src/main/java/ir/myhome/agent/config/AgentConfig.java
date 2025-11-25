package ir.myhome.agent.config;

public final class AgentConfig {

    public String exporterType = "http"; // http | console | none
    public int exporterBatchSize = 50;
    public int exporterCapacity = 10000;
    public String exporterEndpoint = "http://localhost:8080/collect";

    public boolean instrumentationExecutor = true;
    public boolean instrumentationCompletable = true;
    public boolean instrumentationHttpClient = true;
    public boolean instrumentationJdbc = true;
    public boolean instrumentationScheduled = true;
    public boolean instrumentationReactive = false;
    public boolean instrumentationReactor = false;
    public boolean instrumentationVertx = false;
    public boolean instrumentationAkka = false;

    @Override
    public String toString() {
        return "AgentConfig{" + "exporterType='" + exporterType + '\'' + ", exporterBatchSize=" + exporterBatchSize + ", exporterCapacity=" + exporterCapacity + ", exporterEndpoint='" + exporterEndpoint + '\'' + ", instrumentationExecutor=" + instrumentationExecutor + ", instrumentationCompletable=" + instrumentationCompletable + ", instrumentationHttpClient=" + instrumentationHttpClient + ", instrumentationJdbc=" + instrumentationJdbc + ", instrumentationScheduled=" + instrumentationScheduled + ", instrumentationReactive=" + instrumentationReactive + ", instrumentationReactor=" + instrumentationReactor + ", instrumentationVertx=" + instrumentationVertx + ", instrumentationAkka=" + instrumentationAkka + '}';
    }
}

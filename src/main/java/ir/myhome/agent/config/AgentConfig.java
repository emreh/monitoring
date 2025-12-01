package ir.myhome.agent.config;

public final class AgentConfig {
    public String exporterType = "console"; // console | http
    public int exporterBatchSize = 20;
    public int exporterCapacity = 10000;
    public String exporterEndpoint = "http://localhost:8080/collect";

    public boolean instrumentationExecutor = true;
    public boolean instrumentationCompletable = true;
    public boolean instrumentationHttpClient = false;
    public boolean instrumentationJdbc = false;
    public boolean instrumentationScheduled = false;
    public boolean instrumentationReactive = false;

    @Override
    public String toString() {
        return "AgentConfig{" + "exporterType='" + exporterType + '\'' + ", exporterBatchSize=" + exporterBatchSize + ", exporterCapacity=" + exporterCapacity + ", exporterEndpoint='" + exporterEndpoint + '\'' + ", instrumentationExecutor=" + instrumentationExecutor + ", instrumentationCompletable=" + instrumentationCompletable + ", instrumentationHttpClient=" + instrumentationHttpClient + '}';
    }
}

package ir.myhome.agent.ui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import ir.myhome.agent.metrics.AgentMetrics;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public final class StatusServer {

    private final AgentMetrics metrics;
    private final HttpServer server;

    public StatusServer(int port, AgentMetrics metrics) throws IOException {
        this.metrics = metrics;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext("/status", new StatusHandler());
        this.server.setExecutor(null);
    }

    public void start() {
        server.start();
        System.out.println("[StatusServer] started at http://localhost:" + server.getAddress().getPort() + "/status");
    }

    public void stop() {
        server.stop(0);
    }

    class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\n" + "  \"queueSize\": " + metrics.getQueueSize() + ",\n" + "  \"flushedCount\": " + metrics.getFlushedCount() + ",\n" + "  \"lastFlushMs\": " + metrics.getLastFlushEpoch() + "\n" + "}";
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}

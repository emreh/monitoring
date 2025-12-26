package ir.myhome.agent.status;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import ir.myhome.agent.metrics.MetricSnapshot;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

public final class StatusServer {

    private final int port;
    private final BlockingQueue<MetricSnapshot> exportQueue;
    private HttpServer server;

    public StatusServer(int port, BlockingQueue<MetricSnapshot> exportQueue) {
        this.port = port;
        this.exportQueue = exportQueue;
    }

    public void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/status", new StatusHandler());
        server.setExecutor(null); // default
        server.start();
        System.out.println("[StatusServer] started at http://localhost:" + port + "/status");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("[StatusServer] stopped.");
        }
    }

    private class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            try {
                String response = exportQueue.stream().map(MetricSnapshot::toString).collect(Collectors.joining("\n"));
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

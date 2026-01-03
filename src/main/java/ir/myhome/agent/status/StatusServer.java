package ir.myhome.agent.status;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import ir.myhome.agent.core.Span;
import ir.myhome.agent.queue.SpanQueue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class StatusServer {

    private final int port;
    private final SpanQueue<Span> exportQueue; // تغییر به SpanQueue
    private HttpServer server;

    // سازنده اصلاح‌شده که spanQueue را می‌پذیرد
    public StatusServer(int port, SpanQueue<Span> exportQueue) {
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
        public void handle(HttpExchange exchange) throws IOException {
            try {
                // داده‌ها را از exportQueue به صورت دستی می‌گیریم
                List<Span> spans = new ArrayList<>();
                Span span;

                // داده‌ها را یکی یکی از صف می‌گیریم
                while ((span = exportQueue.poll()) != null) {
                    spans.add(span);
                }

                // تبدیل هر span به رشته و پیوستن آن‌ها به هم
                String response = spans.stream().map(Span::toString) // تبدیل هر span به رشته
                        .collect(Collectors.joining("\n"));

                // ارسال هدر پاسخ HTTP
                exchange.sendResponseHeaders(200, response.getBytes().length);

                // ارسال داده‌ها به کاربر
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

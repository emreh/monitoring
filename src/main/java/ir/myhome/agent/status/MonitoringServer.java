package ir.myhome.agent.status;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import ir.myhome.agent.collector.MetricCollector;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

public class MonitoringServer {
    private final int port;
    private final MetricCollector collector;
    private final AtomicLong heartbeat = new AtomicLong(0);
    // متغیر استاتیک برای اینکه از همه جا قابل دسترسی باشد و تعداد کل را نگه دارد
    public static final AtomicLong TOTAL_SPANS = new AtomicLong(0);
    private static long lastTotal = 0;

    public MonitoringServer(int port, MetricCollector collector) {
        this.port = port;
        this.collector = collector;
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/metrics", exchange -> {
            long currentTotal = TOTAL_SPANS.get();
            long throughput = currentTotal - lastTotal; // تعداد اسپان در ثانیه
            lastTotal = currentTotal;

            long currentQueue = (collector != null) ? collector.getExportQueue().size() : 0;

            String json = String.format(
                    "{\"usedMemory\": %.2f, \"queueSize\": %d, \"total\": %d, \"throughput\": %d}",
                    (double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024),
                    currentQueue,
                    currentTotal,
                    throughput
            );

            sendResponse(exchange, json, "application/json");
        });

        server.createContext("/ui", exchange -> {
            sendResponse(exchange, getFinalHtml(), "text/html");
        });

        server.start();
    }

    private void sendResponse(HttpExchange exchange, String response, String contentType) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String getFinalHtml() {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><style>" +
                "body { background: #000; color: #0f0; font-family: monospace; padding: 20px; }" +
                ".box { border: 1px solid #0f0; padding: 15px; margin: 10px 0; background: #050505; }" +
                ".label { color: #888; font-size: 0.8em; }" +
                ".val { font-size: 1.8em; color: #fff; float: right; }" +
                "svg { width: 100%; height: 100px; background: #111; margin-top: 10px; border: 1px solid #222; }" +
                "</style></head><body>" +
                "<h2>> AGENT_CORE_DASHBOARD [PHASE_13]</h2>" +
                "<div class='box'><span class='label'>SYSTEM STATUS:</span> <span class='val' style='color:#0f0'>ACTIVE</span></div>" +
                "<div class='box'><span class='label'>MEMORY USAGE:</span> <span id='m' class='val'>0</span><span class='val'>&nbsp;MB</span></div>" +
                "<div class='box'><span class='label'>TOTAL PROCESSED:</span> <span id='t' class='val'>0</span></div>" +
                "<div class='box'><span class='label'>LIVE QUEUE:</span> <span id='q' class='val'>0</span></div>" +
                "<div class='box'><span class='label'>THROUGHPUT (Metrics/sec):</span> <span id='tp' class='val'>0</span></div>\n" +
                "<div class='box'><span class='label'>LIVE QUEUE (Instant):</span> <span id='q' class='val'>0</span></div>" +
                "<svg viewBox='0 0 500 100'><polyline id='p' fill='none' stroke='#0f0' stroke-width='2'/></svg>" +
                "<script>" +
                "  let hist = [];" +
                "  setInterval(() => {" +
                "    fetch('/api/metrics').then(r => r.json()).then(d => {" +
                "      document.getElementById('m').innerText = d.usedMemory.toFixed(2);" +
                "      document.getElementById('t').innerText = d.total;" +
                "      document.getElementById('q').innerText = d.queueSize;" +
                "      document.getElementById('tp').innerText = d.throughput; // نمایش نرخ زنده\n" +
                "      document.getElementById('q').innerText = d.queueSize;" +
                "      hist.push(d.usedMemory); if(hist.length > 50) hist.shift();" +
                "      let max = Math.max(...hist, 1);" +
                "      let pts = hist.map((v, i) => (i*10) + ',' + (100 - (v/max)*80)).join(' ');" +
                "      document.getElementById('p').setAttribute('points', pts);" +
                "    });" +
                "  }, 1000);" +
                "</script></body></html>";
    }
}
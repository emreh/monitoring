package ir.myhome.agent.exporter;

import ir.myhome.agent.util.JsonSerializer;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class HttpExporter implements Runnable {

    private final String endpoint;
    private final int connectTimeoutMs = 2000;
    private final int readTimeoutMs = 5000;

    public HttpExporter(String endpoint) {
        this.endpoint = endpoint;
    }

    public void export(java.util.Map<String, Object> span) {
        HttpURLConnection conn = null;
        try {
            URL u = new URL(endpoint);
            conn = (HttpURLConnection) u.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(connectTimeoutMs);
            conn.setReadTimeout(readTimeoutMs);
            conn.setDoOutput(true);

            byte[] payload = JsonSerializer.toJson(span).getBytes(StandardCharsets.UTF_8);

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Content-Length", Integer.toString(payload.length));
            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload);
            }
            int code = conn.getResponseCode();
            if (code >= 400) {
                System.err.println("[HttpExporter] remote returned code=" + code);
            }
        } catch (Throwable t) {
            System.err.println("[HttpExporter] send failed: " + t.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    @Override
    public void run() {

    }
}

package ir.myhome.agent.exporter;

import ir.myhome.agent.util.JsonSerializer;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class HttpExporter implements Exporter {

    private final String endpoint;

    public HttpExporter(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void export(Map<String, Object> span) {
        try {
            URL u = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            byte[] payload = JsonSerializer.toJson(span).getBytes(StandardCharsets.UTF_8);

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Content-Length", Integer.toString(payload.length));
            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload);
            }
            conn.getResponseCode();
            conn.disconnect();
        } catch (Throwable t) {
            System.err.println("[HttpExporter] send failed: " + t.getMessage());
        }
    }

    @Override
    public void close() {
    }
}

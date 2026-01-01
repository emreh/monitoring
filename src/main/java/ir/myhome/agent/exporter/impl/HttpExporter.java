package ir.myhome.agent.exporter.impl;

import ir.myhome.agent.core.Span;
import ir.myhome.agent.exporter.AgentExporter;
import ir.myhome.agent.util.JsonSerializer;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class HttpExporter implements AgentExporter {

    private final String endpoint;

    public HttpExporter(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void export(List<Span> spans) {
        if (spans == null || spans.isEmpty()) return;

        try {
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            // Serialize batch
            byte[] payload = JsonSerializer.toJson(spans).getBytes(StandardCharsets.UTF_8);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload);
            }

            int code = conn.getResponseCode();
            if (code != 200) {
                System.err.println("[HttpExporter] send failed: HTTP " + code);
            }

            conn.disconnect();
        } catch (Exception e) {
            System.err.println("[HttpExporter] send failed: " + e.getMessage());
        }
    }
}

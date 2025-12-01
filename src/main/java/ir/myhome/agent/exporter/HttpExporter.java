package ir.myhome.agent.exporter;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class HttpExporter implements Exporter {

    private final String endpoint;

    public HttpExporter(String endpoint, int unusedBatchSize) {
        this.endpoint = endpoint;
    }

    @Override
    public void export(List<String> jsonBatch) {
        if (jsonBatch == null || jsonBatch.isEmpty()) return;

        try {
            String body = "[" + String.join(",", jsonBatch) + "]";
            byte[] payload = body.getBytes(StandardCharsets.UTF_8);
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload);
                os.flush();
            }

            int code = conn.getResponseCode();

            if (code < 200 || code >= 300) {
                System.err.println("[HttpExporter] bad response code: " + code);
            }

            conn.disconnect();
        } catch (Throwable t) {
            System.err.println("[HttpExporter] failed: " + t.getMessage());
        }
    }
}

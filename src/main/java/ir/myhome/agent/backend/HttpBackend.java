package ir.myhome.agent.backend;

import ir.myhome.agent.exporter.SpanExporterBackend;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class HttpBackend implements SpanExporterBackend {

    private final String endpoint;

    public HttpBackend(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void exportBatch(List<String> jsonBatch) throws IOException {
        if (jsonBatch == null || jsonBatch.isEmpty()) return;

        StringBuilder sb = new StringBuilder("[");

        for (int i = 0; i < jsonBatch.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(jsonBatch.get(i));
        }

        sb.append("]");
        byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);

        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setDoOutput(true);
            conn.setConnectTimeout(1500);
            conn.setReadTimeout(2000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body);
                os.flush();
            }

            int code = conn.getResponseCode();

            if (code < 200 || code >= 300) throw new IOException("HTTP backend returned code " + code);
        } finally {
            conn.disconnect();
        }
    }
}

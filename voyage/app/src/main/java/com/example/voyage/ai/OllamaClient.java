package com.example.voyage.ai;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OllamaClient {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface Callback {
        void onSuccess(String response);
        void onError(String error);
    }

    public static void generate(String userPrompt, String systemPrompt,
                                String host, String model, Callback callback) {
        executor.execute(() -> {
            try {
                String fullPrompt = systemPrompt
                        + "\n\nUser: " + userPrompt
                        + "\n\nAssistant:";

                JSONObject body = new JSONObject();
                body.put("model", model);
                body.put("prompt", fullPrompt);
                body.put("stream", false);

                URL url = new URL(host + "/api/generate");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(120000);

                byte[] input = body.toString().getBytes("UTF-8");
                conn.setFixedLengthStreamingMode(input.length);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(input);
                }

                int code = conn.getResponseCode();
                if (code == 200) {
                    StringBuilder sb = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                        String line;
                        while ((line = reader.readLine()) != null) sb.append(line);
                    }
                    JSONObject resp = new JSONObject(sb.toString());
                    String text = resp.optString("response", "").trim();
                    if (text.isEmpty()) {
                        callback.onError("Ollama returned an empty response. Is the model loaded?");
                    } else {
                        callback.onSuccess(text);
                    }
                } else {
                    callback.onError("Ollama HTTP error " + code + ". Is Ollama running?");
                }
                conn.disconnect();

            } catch (ConnectException e) {
                callback.onError("Cannot connect to Ollama at " + host + ".\n\n"
                        + "Make sure Ollama is running:\n  ollama serve\n\n"
                        + "For emulator → host: http://10.0.2.2:11434\n"
                        + "For physical device → use your PC's LAN IP\n"
                        + "  e.g. http://192.168.1.10:11434\n\n"
                        + "Configure host in ⚙️ AI Settings.");
            } catch (SocketTimeoutException e) {
                callback.onError("Ollama timed out. The model may still be loading.\n"
                        + "Try: ollama run " + model + "\nthen send your message again.");
            } catch (Exception e) {
                callback.onError("Error: " + e.getMessage());
            }
        });
    }
}

package com.example.llmlearningassistant;

import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class LLMService {

    private static final String OLLAMA_URL = "http://10.0.2.2:11434/api/generate";
    private static final String MODEL_NAME = "llama3.2:1b";

    public interface LLMCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    public static void generateHint(String question, LLMCallback callback) {
        String prompt = "Give a short helpful hint for this question. Do not give the full answer: " + question;
        callOllama(prompt, callback);
    }

    public static void explainAnswer(String question, String answer, LLMCallback callback) {
        String prompt = "Question: " + question +
                "\nStudent answer: " + answer +
                "\nExplain clearly whether this answer is correct or incorrect in simple student-friendly words.";
        callOllama(prompt, callback);
    }

    /** Send any raw prompt directly to Ollama — used when the caller builds the prompt itself. */
    public static void query(String prompt, LLMCallback callback) {
        callOllama(prompt, callback);
    }

    private static void callOllama(String prompt, LLMCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(OLLAMA_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(120000);
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("model", MODEL_NAME);
                json.put("prompt", prompt);
                json.put("stream", false);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();

                Scanner scanner;
                if (responseCode >= 200 && responseCode < 300) {
                    scanner = new Scanner(conn.getInputStream());
                } else {
                    scanner = new Scanner(conn.getErrorStream());
                }

                StringBuilder result = new StringBuilder();
                while (scanner.hasNext()) {
                    result.append(scanner.nextLine());
                }
                scanner.close();

                if (responseCode >= 200 && responseCode < 300) {
                    JSONObject responseJson = new JSONObject(result.toString());
                    String response = responseJson.optString("response", "No response received.");
                    callback.onSuccess(response);
                } else {
                    callback.onError("Ollama error: " + result);
                }

                conn.disconnect();

            } catch (java.net.SocketTimeoutException e) {
                callback.onError("Request timed out. Ollama is taking too long to respond — try a shorter prompt or check if the model is loaded.");
            } catch (java.net.ConnectException e) {
                callback.onError("Cannot connect to Ollama. Make sure it is running and the model is pulled (ollama pull llama3.2:1b).");
            } catch (Exception e) {
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }
}
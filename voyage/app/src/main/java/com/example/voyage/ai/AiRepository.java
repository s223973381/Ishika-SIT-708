package com.example.voyage.ai;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.voyage.BuildConfig;
import com.example.voyage.util.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AiRepository {

    public enum Mode { OFFLINE, ONLINE, AUTO }

    public interface AiCallback {
        void onResponse(String text, String usedMode);
        void onError(String error);
    }

    private static final String SYSTEM_PROMPT =
            "You are Voyage AI, a smart and friendly travel assistant built into the Voyage app.\n"
            + "You help users with:\n"
            + "- Creating and explaining travel itineraries\n"
            + "- Generating packing checklists for trips\n"
            + "- Summarising journal entries\n"
            + "- Budget saving tips for travel\n"
            + "- Emergency offline guidance\n"
            + "- Basic phrase translation\n"
            + "- Travel Q&A and destination advice\n\n"
            + "Keep responses under 120 words. Be concise, practical, and friendly. "
            + "Use bullet points for lists — no long paragraphs. "
            + "Never repeat the question or add filler phrases. "
            + "If asked about a specific saved trip, note that you don't have access to the "
            + "user's saved data in this version and suggest they check the trip details screen.";

    public static void sendMessage(Context context, String prompt, Mode mode,
                                   AiCallback callback) {
        SessionManager session = new SessionManager(context);
        String host  = session.getOllamaHost();
        String model = session.getOllamaModel();

        boolean online = isOnline(context);
        boolean useOllama;

        switch (mode) {
            case ONLINE:  useOllama = false; break;
            case OFFLINE: useOllama = true;  break;
            case AUTO:
            default:      useOllama = !online; break;
        }

        if (useOllama) {
            OllamaClient.generate(prompt, SYSTEM_PROMPT, host, model, new OllamaClient.Callback() {
                @Override public void onSuccess(String response) {
                    callback.onResponse(response, "offline");
                }
                @Override public void onError(String error) {
                    callback.onError(error);
                }
            });
        } else {
            sendToOpenAi(prompt, callback);
        }
    }

    // ── ChatGPT via OpenAI Responses API ──────────────────────────

    private static void sendToOpenAi(String prompt, AiCallback callback) {
        String apiKey = BuildConfig.OPENAI_API_KEY;
        if (apiKey == null || apiKey.isEmpty()) {
            callback.onError("OpenAI API key not configured.\n\nAdd it to local.properties:\nOPENAI_API_KEY=sk-...");
            return;
        }

        OpenAiRequest request = new OpenAiRequest("gpt-4.1-mini", SYSTEM_PROMPT, prompt);

        OpenAiClient.getApiService()
                .getResponse("Bearer " + apiKey, request)
                .enqueue(new Callback<OpenAiResponse>() {
                    @Override
                    public void onResponse(Call<OpenAiResponse> call,
                                           Response<OpenAiResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String text = response.body().getText();
                            if (text != null && !text.isEmpty()) {
                                callback.onResponse(text, "online");
                            } else {
                                callback.onError("ChatGPT returned an empty response.");
                            }
                        } else {
                            callback.onError(httpErrorMessage(response.code()));
                        }
                    }

                    @Override
                    public void onFailure(Call<OpenAiResponse> call, Throwable t) {
                        callback.onError("Could not reach ChatGPT — check your internet connection.\n"
                                + "Switching to Offline mode will use Ollama instead.");
                    }
                });
    }

    private static String httpErrorMessage(int code) {
        switch (code) {
            case 401: return "Invalid OpenAI API key. Check your key in local.properties.";
            case 429: return "OpenAI rate limit reached. Please wait a moment and try again.";
            case 500:
            case 503: return "OpenAI service is temporarily unavailable. Try again shortly.";
            default:  return "ChatGPT error (HTTP " + code + "). Please try again.";
        }
    }

    // ── Network check ─────────────────────────────────────────────

    public static boolean isOnline(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isConnected();
        } catch (SecurityException e) {
            return false;
        }
    }

    // ── Mode helpers ──────────────────────────────────────────────

    public static Mode fromString(String s) {
        if (s == null) return Mode.AUTO;
        switch (s.toLowerCase()) {
            case "offline": return Mode.OFFLINE;
            case "online":  return Mode.ONLINE;
            default:        return Mode.AUTO;
        }
    }

    public static String toString(Mode mode) {
        switch (mode) {
            case OFFLINE: return "offline";
            case ONLINE:  return "online";
            default:      return "auto";
        }
    }
}

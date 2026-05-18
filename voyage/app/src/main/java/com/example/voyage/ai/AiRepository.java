package com.example.voyage.ai;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.voyage.util.SessionManager;

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
            + "Keep responses concise, practical, and friendly. "
            + "Use bullet points for lists. "
            + "If asked about a specific saved trip, note that you don't have access to the "
            + "user's saved data in this version and suggest they check the trip details screen.";

    public static void sendMessage(Context context, String prompt, Mode mode,
                                   AiCallback callback) {
        SessionManager session = new SessionManager(context);
        String host = session.getOllamaHost();
        String model = session.getOllamaModel();

        boolean online = isOnline(context);
        boolean useOllama;

        switch (mode) {
            case ONLINE:
                useOllama = false;
                break;
            case OFFLINE:
                useOllama = true;
                break;
            case AUTO:
            default:
                useOllama = !online;
                break;
        }

        if (useOllama) {
            OllamaClient.generate(prompt, SYSTEM_PROMPT, host, model, new OllamaClient.Callback() {
                @Override
                public void onSuccess(String response) {
                    callback.onResponse(response, "offline");
                }

                @Override
                public void onError(String error) {
                    callback.onError(error);
                }
            });
        } else {
            // ChatGPT integration — requires user-supplied API key (not implemented yet)
            callback.onError(
                    "ChatGPT (online mode) is not configured yet.\n\n"
                    + "To use AI offline with Ollama:\n"
                    + "1. Install Ollama on your PC: ollama.com\n"
                    + "2. Run: ollama pull llama3.2\n"
                    + "3. Run: ollama serve\n"
                    + "4. Tap ⚙️ to set the Ollama host\n"
                    + "5. Switch mode to 🔌 Offline\n\n"
                    + "ChatGPT support coming in the next update."
            );
        }
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

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

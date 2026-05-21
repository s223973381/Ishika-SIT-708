package com.example.voyage.ai;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherClient {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface Callback {
        void onSuccess(String emoji, String description, String tip, int weatherCode);
        void onError();
    }

    public static void fetch(double lat, double lng, Callback callback) {
        executor.execute(() -> {
            try {
                String urlStr = "https://api.open-meteo.com/v1/forecast"
                        + "?latitude=" + lat
                        + "&longitude=" + lng
                        + "&current=temperature_2m,weather_code"
                        + "&timezone=auto";

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    conn.disconnect();
                    callback.onError();
                    return;
                }

                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                }
                conn.disconnect();

                JSONObject current = new JSONObject(sb.toString()).getJSONObject("current");
                double temp = current.getDouble("temperature_2m");
                int code = current.getInt("weather_code");

                String emoji = emoji(code);
                String desc = description(code) + ", " + Math.round(temp) + "°C";
                String tip = tip(code);

                callback.onSuccess(emoji, desc, tip, code);

            } catch (Exception e) {
                callback.onError();
            }
        });
    }

    private static String emoji(int code) {
        if (code == 0)               return "☀️";
        if (code <= 2)               return "🌤️";
        if (code == 3)               return "☁️";
        if (code <= 48)              return "🌫️";
        if (code <= 55)              return "🌦️";
        if (code <= 65)              return "🌧️";
        if (code <= 77)              return "❄️";
        if (code <= 82)              return "🌧️";
        if (code <= 86)              return "🌨️";
        return "⛈️";
    }

    private static String description(int code) {
        switch (code) {
            case 0:  return "Clear sky";
            case 1:  return "Mainly clear";
            case 2:  return "Partly cloudy";
            case 3:  return "Overcast";
            case 45: return "Foggy";
            case 48: return "Freezing fog";
            case 51: return "Light drizzle";
            case 53: return "Drizzle";
            case 55: return "Heavy drizzle";
            case 61: return "Light rain";
            case 63: return "Rainy";
            case 65: return "Heavy rain";
            case 71: return "Light snow";
            case 73: return "Snowing";
            case 75: return "Heavy snow";
            case 77: return "Snow grains";
            case 80: return "Light showers";
            case 81: return "Rain showers";
            case 82: return "Heavy showers";
            case 85: return "Snow showers";
            case 86: return "Heavy snow showers";
            case 95: return "Thunderstorm";
            case 96:
            case 99: return "Thunderstorm & hail";
            default: return "Cloudy";
        }
    }

    public static String alertTitle(int code) {
        if (code == 0 || code == 1)         return "Perfect weather today!";
        if (code == 2 || code == 3)         return "Light clouds overhead";
        if (code >= 45 && code <= 48)       return "Foggy conditions";
        if (code >= 51 && code <= 55)       return "Light drizzle expected";
        if (code >= 61 && code <= 65)       return "Rain in the forecast";
        if (code >= 71 && code <= 77)       return "Snow expected today";
        if (code >= 80 && code <= 82)       return "Rain showers likely";
        if (code >= 85 && code <= 86)       return "Snow showers expected";
        if (code >= 95)                     return "⚠️ Thunderstorm warning";
        return "Weather update";
    }

    public static String alertSubtitle(int code) {
        if (code == 0 || code == 1)         return "Great day to explore outside";
        if (code == 2 || code == 3)         return "Good for cafés and indoor spots";
        if (code >= 45 && code <= 48)       return "Visibility reduced — drive carefully";
        if (code >= 51 && code <= 55)       return "Pack a light jacket just in case";
        if (code >= 61 && code <= 65)       return "Grab an umbrella before heading out!";
        if (code >= 71 && code <= 77)       return "Dress warmly — snow on the way";
        if (code >= 80 && code <= 82)       return "Scattered showers — stay prepared";
        if (code >= 85 && code <= 86)       return "Snow showers — wrap up warm";
        if (code >= 95)                     return "Stay indoors if possible today";
        return "Check conditions before heading out";
    }

    public static String alertEmoji(int code) {
        if (code == 0 || code == 1)   return "☀️";
        if (code == 2 || code == 3)   return "⛅";
        if (code >= 45 && code <= 48) return "🌫️";
        if (code >= 51 && code <= 65) return "🌧️";
        if (code >= 71 && code <= 77) return "❄️";
        if (code >= 80 && code <= 82) return "⛈️";
        if (code >= 85 && code <= 86) return "🌨️";
        if (code >= 95)               return "⛈️";
        return "🌦️";
    }

    private static String tip(int code) {
        if (code == 0 || code == 1)              return "Great day to explore outside!";
        if (code == 2 || code == 3)              return "Good for indoor activities";
        if (code == 45 || code == 48)            return "Drive carefully in the fog";
        if (code >= 51 && code <= 55)            return "A light jacket recommended";
        if (code >= 61 && code <= 65)            return "Don't forget your umbrella";
        if (code >= 71 && code <= 77)            return "Dress warmly today";
        if (code >= 80 && code <= 82)            return "Scattered showers — stay prepared";
        if (code >= 85 && code <= 86)            return "Snowy outside — wrap up warm";
        if (code >= 95)                          return "Stay indoors if possible";
        return "Check conditions before heading out";
    }
}

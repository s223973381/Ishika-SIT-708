package com.example.voyage.ui.map;

import android.location.Location;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OverpassClient {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final String OVERPASS_URL = "https://overpass-api.de/api/interpreter";

    public interface Callback {
        void onResult(List<NearbyPlace> places);
        void onError(String error);
    }

    public static void fetchNearby(double lat, double lng, String filter, Callback callback) {
        executor.execute(() -> {
            try {
                String query = buildQuery(lat, lng, filter);
                String encoded = "data=" + URLEncoder.encode(query, "UTF-8");

                URL url = new URL(OVERPASS_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("User-Agent", "VoyageApp/1.0");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(20000);

                byte[] body = encoded.getBytes("UTF-8");
                conn.setFixedLengthStreamingMode(body.length);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body);
                }

                if (conn.getResponseCode() != 200) {
                    conn.disconnect();
                    callback.onError("Server error " + conn.getResponseCode());
                    return;
                }

                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                }
                conn.disconnect();

                List<NearbyPlace> places = parseResults(sb.toString(), lat, lng, filter);
                callback.onResult(places);

            } catch (Exception e) {
                callback.onError("Network error: " + e.getMessage());
            }
        });
    }

    private static String buildQuery(double lat, double lng, String filter) {
        int radius;
        String selector;

        switch (filter) {
            case "Food":
                radius = 1500;
                selector = "node[\"amenity\"~\"^(restaurant|cafe|fast_food|food_court|bar|bakery)$\"]"
                         + "(around:" + radius + "," + lat + "," + lng + ");";
                break;
            case "Hotels":
                radius = 2000;
                selector = "node[\"tourism\"~\"^(hotel|hostel|guest_house|motel)$\"]"
                         + "(around:" + radius + "," + lat + "," + lng + ");";
                break;
            case "Attractions":
                radius = 3000;
                selector = "node[\"tourism\"~\"^(attraction|museum|viewpoint|gallery|artwork|theme_park)$\"]"
                         + "(around:" + radius + "," + lat + "," + lng + ");"
                         + "node[\"historic\"](around:" + radius + "," + lat + "," + lng + ");";
                break;
            case "Transport":
                radius = 1000;
                selector = "node[\"public_transport\"~\"^(stop_position|platform)$\"]"
                         + "(around:" + radius + "," + lat + "," + lng + ");"
                         + "node[\"railway\"~\"^(station|halt|tram_stop|subway_entrance)$\"]"
                         + "(around:" + radius + "," + lat + "," + lng + ");"
                         + "node[\"amenity\"~\"^(bus_station|taxi)$\"]"
                         + "(around:" + radius + "," + lat + "," + lng + ");";
                break;
            case "Emergency":
                radius = 3000;
                selector = "node[\"amenity\"~\"^(hospital|pharmacy|police|fire_station|clinic|doctors)$\"]"
                         + "(around:" + radius + "," + lat + "," + lng + ");";
                break;
            default:
                radius = 1500;
                selector = "node[\"amenity\"~\"^(restaurant|cafe)$\"]"
                         + "(around:" + radius + "," + lat + "," + lng + ");";
        }

        return "[out:json][timeout:15];(" + selector + ");out 25;";
    }

    private static List<NearbyPlace> parseResults(String json, double userLat, double userLng,
                                                   String filter) {
        List<NearbyPlace> places = new ArrayList<>();
        try {
            JSONArray elements = new JSONObject(json).getJSONArray("elements");
            for (int i = 0; i < elements.length(); i++) {
                JSONObject el = elements.getJSONObject(i);
                if (!el.has("lat") || !el.has("lon")) continue;

                double lat = el.getDouble("lat");
                double lon = el.getDouble("lon");
                JSONObject tags = el.optJSONObject("tags");
                if (tags == null) continue;

                String name = tags.optString("name", "").trim();
                if (name.isEmpty()) continue;

                String amenity = tags.optString("amenity", "");
                String tourism = tags.optString("tourism", "");
                String historic = tags.optString("historic", "");
                String railway = tags.optString("railway", "");
                String publicTransport = tags.optString("public_transport", "");

                String type = !amenity.isEmpty() ? amenity
                        : !tourism.isEmpty() ? tourism
                        : !historic.isEmpty() ? historic
                        : !railway.isEmpty() ? railway
                        : publicTransport;

                String emoji = emojiFor(type);
                float[] dist = new float[1];
                Location.distanceBetween(userLat, userLng, lat, lon, dist);

                places.add(new NearbyPlace(name, type, emoji, lat, lon, dist[0]));
            }
            places.sort((a, b) -> Float.compare(a.distanceMeters, b.distanceMeters));
        } catch (Exception ignored) {}
        return places;
    }

    static String emojiFor(String type) {
        if (type == null) return "📍";
        switch (type) {
            case "restaurant":      return "🍽️";
            case "cafe":            return "☕";
            case "fast_food":       return "🍔";
            case "bar":             return "🍺";
            case "bakery":          return "🥖";
            case "food_court":      return "🍱";
            case "hotel":           return "🏨";
            case "hostel":          return "🛏️";
            case "guest_house":     return "🏠";
            case "motel":           return "🏩";
            case "attraction":      return "⭐";
            case "museum":          return "🏛️";
            case "viewpoint":       return "🌄";
            case "gallery":         return "🖼️";
            case "artwork":         return "🎨";
            case "theme_park":      return "🎡";
            case "monument":
            case "ruins":
            case "castle":          return "🏰";
            case "hospital":        return "🏥";
            case "pharmacy":        return "💊";
            case "police":          return "🚔";
            case "fire_station":    return "🚒";
            case "clinic":
            case "doctors":         return "⚕️";
            case "station":
            case "halt":
            case "tram_stop":       return "🚉";
            case "subway_entrance": return "🚇";
            case "bus_station":     return "🚌";
            case "taxi":            return "🚕";
            default:                return "📍";
        }
    }
}

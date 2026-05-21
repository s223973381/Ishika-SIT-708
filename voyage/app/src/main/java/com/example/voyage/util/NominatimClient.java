package com.example.voyage.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class NominatimClient {

    /** Synchronous — must be called from a background thread. Returns [lat, lng] or throws. */
    public static double[] geocode(String query, String userAgent) throws Exception {
        String encoded = URLEncoder.encode(query.trim(), "UTF-8");
        URL url = new URL("https://nominatim.openstreetmap.org/search?q="
                + encoded + "&format=json&limit=1");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", userAgent);
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);

        if (conn.getResponseCode() != 200) {
            conn.disconnect();
            throw new Exception("HTTP " + conn.getResponseCode());
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        conn.disconnect();

        JSONArray results = new JSONArray(sb.toString());
        if (results.length() == 0) throw new Exception("No results for: " + query);

        JSONObject first = results.getJSONObject(0);
        return new double[]{
                Double.parseDouble(first.getString("lat")),
                Double.parseDouble(first.getString("lon"))
        };
    }
}

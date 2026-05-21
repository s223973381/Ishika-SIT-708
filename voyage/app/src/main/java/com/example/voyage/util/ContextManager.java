package com.example.voyage.util;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;

import java.util.Calendar;

public class ContextManager {

    public static boolean isOnline(Context ctx) {
        try {
            ConnectivityManager cm = (ConnectivityManager)
                    ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isConnected();
        } catch (SecurityException e) {
            return true;
        }
    }

    public static int getBatteryPercent(Context ctx) {
        try {
            Intent status = ctx.registerReceiver(null,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            if (status == null) return 100;
            int level = status.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = status.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            if (level < 0 || scale <= 0) return 100;
            return (int) ((level / (float) scale) * 100);
        } catch (Exception e) {
            return 100;
        }
    }

    public static int getHour() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }

    public static String timeLabel(int hour) {
        if (hour >= 6  && hour < 12) return "morning";
        if (hour >= 12 && hour < 17) return "afternoon";
        if (hour >= 17 && hour < 22) return "evening";
        return "night";
    }

    /** Best Overpass category for suggestion card 1 based on time, weather, travel style. */
    public static String pickSuggestionFilter(int hour, int weatherCode, String travelStyle) {
        if (hour >= 22 || hour < 6)  return "Transport";   // late night
        if (hour < 12)               return "Food";         // morning: breakfast/coffee
        if (hour >= 17)              return "Food";         // evening: dinner
        // afternoon
        if (weatherCode >= 51)       return "Attractions";  // rainy: indoor spots
        if ("budget".equalsIgnoreCase(travelStyle)) return "Food";
        return "Attractions"; // adventure / culture / relax / default
    }

    public static String suggestionEmoji(int hour, int weatherCode, String travelStyle) {
        if (hour >= 22 || hour < 6)  return "🚌";
        if (hour < 12)               return "☕";
        if (hour >= 17)              return "🍽️";
        if (weatherCode >= 51)       return "🏛️";
        if ("adventure".equalsIgnoreCase(travelStyle)) return "🧗";
        if ("culture".equalsIgnoreCase(travelStyle))   return "🏛️";
        if ("budget".equalsIgnoreCase(travelStyle))    return "💸";
        return "⭐";
    }

    public static String suggestionTitle(int hour, int weatherCode, String travelStyle) {
        if (hour >= 22 || hour < 6)  return "Transport nearby";
        if (hour < 12)               return "Breakfast & coffee nearby";
        if (hour >= 17)              return "Dinner spots nearby";
        if (weatherCode >= 51)       return "Indoor attractions nearby";
        if ("adventure".equalsIgnoreCase(travelStyle)) return "Adventure spots nearby";
        if ("culture".equalsIgnoreCase(travelStyle))   return "Cultural attractions nearby";
        if ("budget".equalsIgnoreCase(travelStyle))    return "Budget eats nearby";
        return "Attractions nearby";
    }

    /** Returns a styled chip label for the travel-style chip, or null if no style. */
    public static String travelStyleChipLabel(String style) {
        if (style == null || style.isEmpty()) return null;
        switch (style.toLowerCase()) {
            case "adventure": return "🧗 Adventure mode";
            case "culture":   return "🏛️ Culture mode";
            case "budget":    return "💸 Budget mode";
            case "relax":     return "😌 Relax mode";
            default:          return null;
        }
    }

    /** Context prefix prepended to AI prompts for personalised responses. */
    public static String buildAiContext(String city, String weatherDesc,
                                        int hour, String travelStyle) {
        StringBuilder sb = new StringBuilder("[Context: ");
        if (city != null && !city.isEmpty()) {
            sb.append("Location: ").append(city).append(". ");
        }
        if (weatherDesc != null && !weatherDesc.isEmpty()) {
            sb.append("Weather: ").append(weatherDesc).append(". ");
        }
        sb.append("Time of day: ").append(timeLabel(hour)).append(". ");
        if (travelStyle != null && !travelStyle.isEmpty()) {
            sb.append("Travel style: ").append(travelStyle).append(". ");
        }
        sb.append("Use this context to personalise your response.]\n\n");
        return sb.toString();
    }
}

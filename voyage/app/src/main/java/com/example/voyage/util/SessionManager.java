package com.example.voyage.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREFS_NAME = "voyage_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_ONBOARDING_DONE = "onboarding_done";
    private static final String KEY_IS_GUEST = "is_guest";
    private static final String KEY_AI_MODE = "ai_mode";
    private static final String KEY_TRAVEL_STYLE = "travel_style";
    private static final String KEY_OLLAMA_HOST = "ollama_host";
    private static final String KEY_OLLAMA_MODEL = "ollama_model";
    private static final String KEY_CURRENCY = "currency";
    private static final String KEY_DISTANCE_UNIT = "distance_unit";
    private static final String KEY_DATE_FORMAT = "date_format";
    private static final String KEY_NOTIF_ENABLED = "notif_enabled";
    private static final String KEY_NOTIF_REMINDERS = "notif_reminders";
    private static final String KEY_NOTIF_TIPS = "notif_tips";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserSession(int userId, String name, String email) {
        prefs.edit()
                .putInt(KEY_USER_ID, userId)
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_EMAIL, email)
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putBoolean(KEY_IS_GUEST, false)
                .apply();
    }

    public void saveGuestSession() {
        prefs.edit()
                .putInt(KEY_USER_ID, -1)
                .putString(KEY_USER_NAME, "Traveller")
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putBoolean(KEY_IS_GUEST, true)
                .apply();
    }

    public void setOnboardingDone() {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, true).apply();
    }

    public boolean isOnboardingDone() {
        return prefs.getBoolean(KEY_ONBOARDING_DONE, false);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public boolean isGuest() {
        return prefs.getBoolean(KEY_IS_GUEST, false);
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "Traveller");
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public String getAiMode() {
        return prefs.getString(KEY_AI_MODE, "auto");
    }

    public void setAiMode(String mode) {
        prefs.edit().putString(KEY_AI_MODE, mode).apply();
    }

    public String getTravelStyle() {
        return prefs.getString(KEY_TRAVEL_STYLE, "");
    }

    public void setTravelStyle(String style) {
        prefs.edit().putString(KEY_TRAVEL_STYLE, style).apply();
    }

    public String getOllamaHost() {
        return prefs.getString(KEY_OLLAMA_HOST, "http://10.0.2.2:11434");
    }

    public void setOllamaHost(String host) {
        prefs.edit().putString(KEY_OLLAMA_HOST, host).apply();
    }

    public String getOllamaModel() {
        return prefs.getString(KEY_OLLAMA_MODEL, "llama3.2:1b");
    }

    public void setOllamaModel(String model) {
        prefs.edit().putString(KEY_OLLAMA_MODEL, model).apply();
    }

    public void updateUserName(String name) {
        prefs.edit().putString(KEY_USER_NAME, name).apply();
    }

    public String getCurrency() { return prefs.getString(KEY_CURRENCY, "USD"); }
    public void setCurrency(String c) { prefs.edit().putString(KEY_CURRENCY, c).apply(); }

    public String getDistanceUnit() { return prefs.getString(KEY_DISTANCE_UNIT, "km"); }
    public void setDistanceUnit(String u) { prefs.edit().putString(KEY_DISTANCE_UNIT, u).apply(); }

    public String getDateFormat() { return prefs.getString(KEY_DATE_FORMAT, "DD/MM/YYYY"); }
    public void setDateFormat(String f) { prefs.edit().putString(KEY_DATE_FORMAT, f).apply(); }

    public boolean isNotifEnabled() { return prefs.getBoolean(KEY_NOTIF_ENABLED, true); }
    public void setNotifEnabled(boolean v) { prefs.edit().putBoolean(KEY_NOTIF_ENABLED, v).apply(); }

    public boolean isNotifReminders() { return prefs.getBoolean(KEY_NOTIF_REMINDERS, true); }
    public void setNotifReminders(boolean v) { prefs.edit().putBoolean(KEY_NOTIF_REMINDERS, v).apply(); }

    public boolean isNotifTips() { return prefs.getBoolean(KEY_NOTIF_TIPS, true); }
    public void setNotifTips(boolean v) { prefs.edit().putBoolean(KEY_NOTIF_TIPS, v).apply(); }

    public void logout() {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .putBoolean(KEY_IS_GUEST, false)
                .putInt(KEY_USER_ID, -1)
                .putString(KEY_USER_NAME, "")
                .putString(KEY_USER_EMAIL, "")
                .apply();
    }
}

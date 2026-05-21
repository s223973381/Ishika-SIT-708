package com.example.voyage.util;

/** Shared in-memory context populated by HomeFragment; read by AI chat and other screens. */
public class AppContext {
    public static volatile String currentCity = "";
    public static volatile String weatherDescription = "";
    public static volatile int    weatherCode = 0;
    public static volatile double userLat = 0;
    public static volatile double userLng = 0;
}

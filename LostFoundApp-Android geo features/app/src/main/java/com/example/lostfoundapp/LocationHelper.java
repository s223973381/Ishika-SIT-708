package com.example.lostfoundapp;

import android.location.Location;

public class LocationHelper {

    public static float distanceKm(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0] / 1000f;
    }

    public static boolean hasCoordinates(double latitude, double longitude) {
        return latitude != 0 || longitude != 0;
    }
}

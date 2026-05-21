package com.example.voyage.ui.map;

public class NearbyPlace {
    public final String name;
    public final String type;
    public final String emoji;
    public final double lat;
    public final double lng;
    public float distanceMeters;

    public NearbyPlace(String name, String type, String emoji,
                       double lat, double lng, float distanceMeters) {
        this.name = name;
        this.type = type;
        this.emoji = emoji;
        this.lat = lat;
        this.lng = lng;
        this.distanceMeters = distanceMeters;
    }

    public String getFormattedDistance() {
        if (distanceMeters < 1000) {
            return (int) distanceMeters + "m";
        } else {
            return String.format("%.1fkm", distanceMeters / 1000.0);
        }
    }
}

package com.example.voyage.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "places")
public class Place {
    @PrimaryKey(autoGenerate = true)
    public int placeId;
    public Integer tripId;    // nullable — null means general saved place
    public String name;
    public String category;   // food, hotel, attraction, emergency, transport
    public String address;
    public double latitude;
    public double longitude;
    public float rating;
    public String notes;
    public boolean isSaved;
}

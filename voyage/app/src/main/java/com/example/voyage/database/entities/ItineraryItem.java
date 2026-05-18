package com.example.voyage.database.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "itinerary_items",
    foreignKeys = @ForeignKey(
        entity = Trip.class,
        parentColumns = "tripId",
        childColumns = "tripId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = @Index("tripId")
)
public class ItineraryItem {
    @PrimaryKey(autoGenerate = true)
    public int itemId;
    public int tripId;
    public int dayNumber;
    public String timeSlot;   // "morning", "afternoon", "evening"
    public String title;
    public String description;
    public String locationName;
    public double latitude;
    public double longitude;
    public String startTime;
    public String endTime;
    public double estimatedCost;
    public int orderIndex;
}

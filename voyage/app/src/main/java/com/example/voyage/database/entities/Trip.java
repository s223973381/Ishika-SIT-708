package com.example.voyage.database.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "trips",
    foreignKeys = @ForeignKey(
        entity = User.class,
        parentColumns = "userId",
        childColumns = "userId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = @Index("userId")
)
public class Trip {
    @PrimaryKey(autoGenerate = true)
    public int tripId;
    public int userId;
    public String title;
    public String destination;
    public String startDate;
    public String endDate;
    public int days;
    public double budget;
    public String travelStyle;
    public String interests;
    public String aiMode;
    public boolean isOfflineSaved;
    public boolean isCompleted;
    public long createdAt;
}

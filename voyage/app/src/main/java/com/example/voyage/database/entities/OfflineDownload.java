package com.example.voyage.database.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "offline_downloads",
    foreignKeys = @ForeignKey(
        entity = Trip.class,
        parentColumns = "tripId",
        childColumns = "tripId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = @Index("tripId")
)
public class OfflineDownload {
    @PrimaryKey(autoGenerate = true)
    public int downloadId;
    public int tripId;
    public boolean itinerarySaved;
    public boolean mapSaved;
    public boolean aiResponsesSaved;
    public long lastDownloadedAt;
}

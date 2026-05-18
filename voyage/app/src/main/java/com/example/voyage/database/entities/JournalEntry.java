package com.example.voyage.database.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "journal_entries",
    foreignKeys = @ForeignKey(
        entity = Trip.class,
        parentColumns = "tripId",
        childColumns = "tripId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = @Index("tripId")
)
public class JournalEntry {
    @PrimaryKey(autoGenerate = true)
    public int journalId;
    public int tripId;
    public String title;
    public String content;
    public String imagePath;
    public String mood;
    public String date;
    public String aiSummary;
}

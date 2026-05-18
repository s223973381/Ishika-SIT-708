package com.example.voyage.database.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "checklist_items",
    foreignKeys = @ForeignKey(
        entity = Trip.class,
        parentColumns = "tripId",
        childColumns = "tripId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = @Index("tripId")
)
public class ChecklistItem {
    @PrimaryKey(autoGenerate = true)
    public int checklistId;
    public int tripId;
    public String itemName;
    public String category;
    public boolean isChecked;
    public boolean isAiGenerated;
}

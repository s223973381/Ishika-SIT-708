package com.example.voyage.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "packing_lists")
public class PackingList {
    @PrimaryKey(autoGenerate = true)
    public int listId;
    public Integer tripId; // null = global list, non-null = trip-specific
    public String listName;
    public long createdAt;
}

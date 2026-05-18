package com.example.voyage.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int userId;
    public String name;
    public String email;
    public String travelStyle;
    public String preferredAiMode;
    public String homeLocation;
    public long createdAt;
}

package com.example.voyage.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ai_cache")
public class AiCache {
    @PrimaryKey(autoGenerate = true)
    public int cacheId;
    public String promptHash;
    public String promptText;
    public String responseText;
    public String aiMode;
    public long createdAt;
    public long expiresAt;
}

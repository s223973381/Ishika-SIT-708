package com.example.voyage.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ai_chat_messages")
public class AiChatMessage {
    @PrimaryKey(autoGenerate = true)
    public int messageId;
    public Integer tripId;    // nullable — null = global chat
    public String sender;     // "user" or "ai"
    public String message;
    public String aiMode;     // "offline", "online", "auto"
    public long timestamp;
}

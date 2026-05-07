package com.example.llmchatbot.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_messages")
public class ChatMessage {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "username")
    private String username;

    @ColumnInfo(name = "message")
    private String message;

    @ColumnInfo(name = "is_user")
    private boolean isUser;

    @ColumnInfo(name = "timestamp")
    private long timestamp;

    public ChatMessage(String username, String message, boolean isUser, long timestamp) {
        this.username = username;
        this.message = message;
        this.isUser = isUser;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isUser() { return isUser; }
    public void setUser(boolean user) { isUser = user; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}

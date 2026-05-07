package com.example.llmchatbot.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ChatDao {

    @Insert
    void insertMessage(ChatMessage message);

    @Query("SELECT * FROM chat_messages WHERE username = :username ORDER BY timestamp ASC")
    List<ChatMessage> getMessagesForUser(String username);

    @Query("DELETE FROM chat_messages WHERE username = :username")
    void deleteMessagesForUser(String username);

    @Query("DELETE FROM chat_messages")
    void deleteAllMessages();
}

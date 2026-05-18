package com.example.voyage.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.voyage.database.entities.AiChatMessage;
import java.util.List;

@Dao
public interface AiChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(AiChatMessage message);

    @Delete
    void delete(AiChatMessage message);

    @Query("SELECT * FROM ai_chat_messages WHERE tripId IS NULL ORDER BY timestamp ASC")
    LiveData<List<AiChatMessage>> getGlobalMessages();

    @Query("SELECT * FROM ai_chat_messages WHERE tripId = :tripId ORDER BY timestamp ASC")
    LiveData<List<AiChatMessage>> getMessagesForTrip(int tripId);

    @Query("DELETE FROM ai_chat_messages WHERE tripId IS NULL")
    void clearGlobalChat();

    @Query("DELETE FROM ai_chat_messages WHERE tripId = :tripId")
    void clearChatForTrip(int tripId);
}

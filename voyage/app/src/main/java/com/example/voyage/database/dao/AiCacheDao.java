package com.example.voyage.database.dao;

import androidx.room.*;
import com.example.voyage.database.entities.AiCache;

@Dao
public interface AiCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(AiCache cache);

    @Query("SELECT * FROM ai_cache WHERE promptHash = :hash AND expiresAt > :now LIMIT 1")
    AiCache getCachedResponse(String hash, long now);

    @Query("DELETE FROM ai_cache WHERE expiresAt < :now")
    void clearExpired(long now);

    @Query("DELETE FROM ai_cache")
    void clearAll();
}

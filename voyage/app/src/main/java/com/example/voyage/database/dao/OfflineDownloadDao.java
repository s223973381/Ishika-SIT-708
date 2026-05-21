package com.example.voyage.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.voyage.database.entities.OfflineDownload;
import java.util.List;

@Dao
public interface OfflineDownloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(OfflineDownload download);

    @Update
    void update(OfflineDownload download);

    @Query("SELECT * FROM offline_downloads WHERE tripId = :tripId LIMIT 1")
    LiveData<OfflineDownload> getDownloadForTrip(int tripId);

    @Query("SELECT * FROM offline_downloads")
    LiveData<List<OfflineDownload>> getAllDownloads();

    @Query("SELECT * FROM offline_downloads WHERE tripId = :tripId LIMIT 1")
    OfflineDownload getDownloadForTripSync(int tripId);

    @Query("SELECT * FROM offline_downloads WHERE mapSaved = 1")
    List<OfflineDownload> getAllMapDownloadsSync();

    @Query("UPDATE offline_downloads SET mapSaved = 0")
    void clearAllMapSavedFlags();

    @Query("DELETE FROM offline_downloads WHERE tripId = :tripId")
    void deleteForTrip(int tripId);
}

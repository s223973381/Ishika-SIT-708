package com.example.voyage.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.voyage.database.entities.JournalEntry;
import java.util.List;

@Dao
public interface JournalEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(JournalEntry entry);

    @Update
    void update(JournalEntry entry);

    @Delete
    void delete(JournalEntry entry);

    @Query("SELECT * FROM journal_entries WHERE tripId = :tripId ORDER BY date DESC")
    LiveData<List<JournalEntry>> getEntriesForTrip(int tripId);

    @Query("SELECT * FROM journal_entries ORDER BY date DESC")
    LiveData<List<JournalEntry>> getAllEntries();

    @Query("SELECT * FROM journal_entries WHERE journalId = :id")
    LiveData<JournalEntry> getEntryById(int id);

    @Query("DELETE FROM journal_entries WHERE journalId = :id")
    void deleteById(int id);
}

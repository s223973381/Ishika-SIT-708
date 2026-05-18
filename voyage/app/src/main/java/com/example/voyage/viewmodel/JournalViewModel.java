package com.example.voyage.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.voyage.database.AppDatabase;
import com.example.voyage.database.dao.JournalEntryDao;
import com.example.voyage.database.entities.JournalEntry;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JournalViewModel extends AndroidViewModel {
    private final JournalEntryDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public JournalViewModel(@NonNull Application application) {
        super(application);
        dao = AppDatabase.getInstance(application).journalEntryDao();
    }

    public LiveData<List<JournalEntry>> getEntriesForTrip(int tripId) {
        return dao.getEntriesForTrip(tripId);
    }

    public LiveData<List<JournalEntry>> getAllEntries() {
        return dao.getAllEntries();
    }

    public LiveData<JournalEntry> getEntryById(int id) {
        return dao.getEntryById(id);
    }

    public void insertEntry(JournalEntry entry) {
        executor.execute(() -> dao.insert(entry));
    }

    public void updateEntry(JournalEntry entry) {
        executor.execute(() -> dao.update(entry));
    }

    public void deleteEntry(JournalEntry entry) {
        executor.execute(() -> dao.delete(entry));
    }
}

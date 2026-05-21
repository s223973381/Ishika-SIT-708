package com.example.voyage.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.voyage.database.AppDatabase;
import com.example.voyage.database.dao.AiChatMessageDao;
import com.example.voyage.database.entities.AiChatMessage;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AiChatViewModel extends AndroidViewModel {
    private final AiChatMessageDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public AiChatViewModel(@NonNull Application application) {
        super(application);
        dao = AppDatabase.getInstance(application).aiChatMessageDao();
    }

    public LiveData<List<AiChatMessage>> getGlobalMessages() {
        return dao.getGlobalMessages();
    }

    public LiveData<List<AiChatMessage>> getMessagesForTrip(int tripId) {
        return dao.getMessagesForTrip(tripId);
    }

    public void sendMessage(AiChatMessage message) {
        executor.execute(() -> dao.insert(message));
    }

    public void clearGlobalChat() {
        executor.execute(dao::clearGlobalChat);
    }

    public void clearTripChat(int tripId) {
        executor.execute(() -> dao.clearChatForTrip(tripId));
    }

    public void deleteOlderThan(long cutoffMs) {
        executor.execute(() -> dao.deleteOlderThan(cutoffMs));
    }
}

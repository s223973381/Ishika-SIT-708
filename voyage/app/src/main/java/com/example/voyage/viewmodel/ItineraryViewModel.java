package com.example.voyage.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.voyage.database.AppDatabase;
import com.example.voyage.database.dao.ItineraryItemDao;
import com.example.voyage.database.entities.ItineraryItem;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ItineraryViewModel extends AndroidViewModel {
    private final ItineraryItemDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ItineraryViewModel(@NonNull Application application) {
        super(application);
        dao = AppDatabase.getInstance(application).itineraryItemDao();
    }

    public LiveData<List<ItineraryItem>> getItemsForTrip(int tripId) {
        return dao.getItemsByTrip(tripId);
    }

    public void insertItem(ItineraryItem item) {
        executor.execute(() -> dao.insert(item));
    }

    public void updateItem(ItineraryItem item) {
        executor.execute(() -> dao.update(item));
    }

    public void deleteItem(ItineraryItem item) {
        executor.execute(() -> dao.delete(item));
    }

    public void replaceItems(int tripId, List<ItineraryItem> items) {
        executor.execute(() -> {
            dao.deleteAllForTrip(tripId);
            dao.insertAll(items);
        });
    }
}

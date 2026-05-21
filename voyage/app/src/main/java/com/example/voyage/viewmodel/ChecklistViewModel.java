package com.example.voyage.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.voyage.database.AppDatabase;
import com.example.voyage.database.dao.ChecklistItemDao;
import com.example.voyage.database.entities.ChecklistItem;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChecklistViewModel extends AndroidViewModel {
    private final ChecklistItemDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ChecklistViewModel(@NonNull Application application) {
        super(application);
        dao = AppDatabase.getInstance(application).checklistItemDao();
    }

    public LiveData<List<ChecklistItem>> getItemsForTrip(int tripId) {
        return dao.getItemsForTrip(tripId);
    }

    public void insertItem(ChecklistItem item) {
        executor.execute(() -> dao.insert(item));
    }

    public void insertAll(List<ChecklistItem> items) {
        executor.execute(() -> dao.insertAll(items));
    }

    public void setChecked(int id, boolean checked) {
        executor.execute(() -> dao.setChecked(id, checked));
    }

    public void updateItem(ChecklistItem item) {
        executor.execute(() -> dao.update(item));
    }

    public void deleteItem(int id) {
        executor.execute(() -> dao.deleteById(id));
    }
}

package com.example.voyage.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.voyage.database.AppDatabase;
import com.example.voyage.database.dao.PackingListDao;
import com.example.voyage.database.dao.PackingListItemDao;
import com.example.voyage.database.entities.PackingList;
import com.example.voyage.database.entities.PackingListItem;
import com.example.voyage.database.pojo.PackingListWithCount;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PackingListViewModel extends AndroidViewModel {

    private final PackingListDao listDao;
    private final PackingListItemDao itemDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public PackingListViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        listDao = db.packingListDao();
        itemDao = db.packingListItemDao();
    }

    public LiveData<List<PackingListWithCount>> getAllLists() {
        return listDao.getAllWithCounts();
    }

    public void insertList(PackingList list, OnInsertCallback callback) {
        executor.execute(() -> {
            long id = listDao.insert(list);
            if (callback != null) callback.onInserted((int) id);
        });
    }

    public void deleteList(int listId) {
        executor.execute(() -> {
            PackingList list = listDao.getById(listId);
            if (list != null) listDao.delete(list);
        });
    }

    public LiveData<List<PackingListItem>> getItemsForList(int listId) {
        return itemDao.getItemsForList(listId);
    }

    public void insertItem(PackingListItem item) {
        executor.execute(() -> itemDao.insert(item));
    }

    public void insertAll(List<PackingListItem> items) {
        executor.execute(() -> itemDao.insertAll(items));
    }

    public void setPacked(int id, boolean packed) {
        executor.execute(() -> itemDao.setPacked(id, packed));
    }

    public void deleteItem(int id) {
        executor.execute(() -> itemDao.deleteById(id));
    }

    public interface OnInsertCallback {
        void onInserted(int listId);
    }
}

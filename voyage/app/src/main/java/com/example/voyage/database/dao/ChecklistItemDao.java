package com.example.voyage.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.voyage.database.entities.ChecklistItem;
import java.util.List;

@Dao
public interface ChecklistItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ChecklistItem item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ChecklistItem> items);

    @Update
    void update(ChecklistItem item);

    @Delete
    void delete(ChecklistItem item);

    @Query("SELECT * FROM checklist_items WHERE tripId = :tripId ORDER BY category ASC, itemName ASC")
    LiveData<List<ChecklistItem>> getItemsForTrip(int tripId);

    @Query("SELECT * FROM checklist_items WHERE tripId = :tripId AND isChecked = 0")
    LiveData<List<ChecklistItem>> getUncheckedItems(int tripId);

    @Query("UPDATE checklist_items SET isChecked = :checked WHERE checklistId = :id")
    void setChecked(int id, boolean checked);

    @Query("DELETE FROM checklist_items WHERE checklistId = :id")
    void deleteById(int id);
}

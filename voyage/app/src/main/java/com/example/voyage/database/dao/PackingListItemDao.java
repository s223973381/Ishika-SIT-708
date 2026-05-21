package com.example.voyage.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import com.example.voyage.database.entities.PackingListItem;

import java.util.List;

@Dao
public interface PackingListItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PackingListItem item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PackingListItem> items);

    @Delete
    void delete(PackingListItem item);

    @Query("SELECT * FROM packing_list_items WHERE listId = :listId ORDER BY category ASC, itemName ASC")
    LiveData<List<PackingListItem>> getItemsForList(int listId);

    @Query("UPDATE packing_list_items SET isPacked = :packed WHERE itemId = :id")
    void setPacked(int id, boolean packed);

    @Query("DELETE FROM packing_list_items WHERE itemId = :id")
    void deleteById(int id);
}

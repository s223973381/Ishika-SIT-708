package com.example.voyage.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import com.example.voyage.database.entities.PackingList;
import com.example.voyage.database.pojo.PackingListWithCount;

import java.util.List;

@Dao
public interface PackingListDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(PackingList list);

    @Delete
    void delete(PackingList list);

    @Query("SELECT pl.listId, pl.tripId, pl.listName, pl.createdAt, " +
           "COUNT(pli.itemId) as totalItems, " +
           "COALESCE(SUM(CASE WHEN pli.isPacked = 1 THEN 1 ELSE 0 END), 0) as packedItems " +
           "FROM packing_lists pl " +
           "LEFT JOIN packing_list_items pli ON pl.listId = pli.listId " +
           "GROUP BY pl.listId " +
           "ORDER BY pl.createdAt DESC")
    LiveData<List<PackingListWithCount>> getAllWithCounts();

    @Query("SELECT * FROM packing_lists WHERE listId = :id")
    PackingList getById(int id);
}

package com.example.voyage.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.voyage.database.entities.ItineraryItem;
import java.util.List;

@Dao
public interface ItineraryItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ItineraryItem item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ItineraryItem> items);

    @Update
    void update(ItineraryItem item);

    @Delete
    void delete(ItineraryItem item);

    @Query("SELECT * FROM itinerary_items WHERE tripId = :tripId ORDER BY dayNumber ASC, orderIndex ASC")
    LiveData<List<ItineraryItem>> getItemsByTrip(int tripId);

    @Query("SELECT * FROM itinerary_items WHERE tripId = :tripId AND dayNumber = :day ORDER BY orderIndex ASC")
    LiveData<List<ItineraryItem>> getItemsByDay(int tripId, int day);

    @Query("DELETE FROM itinerary_items WHERE tripId = :tripId")
    void deleteAllForTrip(int tripId);
}

package com.example.voyage.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.voyage.database.entities.Place;
import java.util.List;

@Dao
public interface PlaceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Place place);

    @Update
    void update(Place place);

    @Delete
    void delete(Place place);

    @Query("SELECT * FROM places WHERE tripId = :tripId")
    LiveData<List<Place>> getPlacesForTrip(int tripId);

    @Query("SELECT * FROM places WHERE tripId IS NULL AND isSaved = 1")
    LiveData<List<Place>> getSavedPlaces();

    @Query("SELECT * FROM places WHERE category = :category")
    LiveData<List<Place>> getPlacesByCategory(String category);

    @Query("DELETE FROM places WHERE placeId = :placeId")
    void deletePlaceById(int placeId);
}

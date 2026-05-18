package com.example.voyage.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.voyage.database.entities.Trip;
import java.util.List;

@Dao
public interface TripDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Trip trip);

    @Update
    void update(Trip trip);

    @Delete
    void delete(Trip trip);

    @Query("SELECT * FROM trips WHERE userId = :userId AND isCompleted = 0 ORDER BY createdAt DESC")
    LiveData<List<Trip>> getUpcomingTrips(int userId);

    @Query("SELECT * FROM trips WHERE userId = :userId AND isCompleted = 1 ORDER BY createdAt DESC")
    LiveData<List<Trip>> getCompletedTrips(int userId);

    @Query("SELECT * FROM trips WHERE userId = :userId ORDER BY createdAt DESC")
    LiveData<List<Trip>> getAllTrips(int userId);

    @Query("SELECT * FROM trips WHERE tripId = :tripId")
    LiveData<Trip> getTripById(int tripId);

    @Query("SELECT * FROM trips WHERE userId = :userId AND isCompleted = 0 ORDER BY createdAt DESC LIMIT 1")
    LiveData<Trip> getLatestUpcomingTrip(int userId);

    @Query("DELETE FROM trips WHERE tripId = :tripId")
    void deleteTripById(int tripId);
}

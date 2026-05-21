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

    @Query("SELECT * FROM trips WHERE userId = :userId AND isCompleted = 0 ORDER BY startDate ASC")
    LiveData<List<Trip>> getUpcomingTrips(int userId);

    @Query("SELECT * FROM trips WHERE userId = :userId AND isCompleted = 1 ORDER BY startDate DESC")
    LiveData<List<Trip>> getCompletedTrips(int userId);

    @Query("SELECT * FROM trips WHERE userId = :userId ORDER BY startDate ASC")
    LiveData<List<Trip>> getAllTrips(int userId);

    @Query("SELECT * FROM trips WHERE tripId = :tripId")
    LiveData<Trip> getTripById(int tripId);

    // Returns the trip closest to today: nearest future trip first, then nearest past trip.
    @Query("SELECT * FROM trips WHERE userId = :userId AND isCompleted = 0 " +
           "ORDER BY CASE WHEN startDate >= :today THEN 0 ELSE 1 END ASC, " +
           "ABS(julianday(startDate) - julianday(:today)) ASC LIMIT 1")
    LiveData<Trip> getLatestUpcomingTrip(int userId, String today);

    @Query("SELECT * FROM trips WHERE tripId = :tripId")
    Trip getTripByIdSync(int tripId);

    @Query("DELETE FROM trips WHERE tripId = :tripId")
    void deleteTripById(int tripId);
}

package com.example.voyage.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.voyage.database.AppDatabase;
import com.example.voyage.database.dao.TripDao;
import com.example.voyage.database.entities.Trip;
import com.example.voyage.util.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TripViewModel extends AndroidViewModel {
    private final TripDao tripDao;
    private final SessionManager session;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public TripViewModel(@NonNull Application application) {
        super(application);
        tripDao = AppDatabase.getInstance(application).tripDao();
        session = new SessionManager(application);
    }

    public LiveData<List<Trip>> getUpcomingTrips() {
        return tripDao.getUpcomingTrips(session.getUserId());
    }

    public LiveData<List<Trip>> getCompletedTrips() {
        return tripDao.getCompletedTrips(session.getUserId());
    }

    public LiveData<List<Trip>> getAllTrips() {
        return tripDao.getAllTrips(session.getUserId());
    }

    public LiveData<Trip> getTripById(int tripId) {
        return tripDao.getTripById(tripId);
    }

    public LiveData<Trip> getLatestUpcomingTrip() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        return tripDao.getLatestUpcomingTrip(session.getUserId(), today);
    }

    public void insertTrip(Trip trip, OnInsertCallback callback) {
        executor.execute(() -> {
            long id = tripDao.insert(trip);
            if (callback != null) callback.onInserted((int) id);
        });
    }

    public void updateTrip(Trip trip) {
        executor.execute(() -> tripDao.update(trip));
    }

    public void deleteTrip(int tripId) {
        executor.execute(() -> tripDao.deleteTripById(tripId));
    }

    public interface OnInsertCallback {
        void onInserted(int tripId);
    }
}

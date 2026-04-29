package com.example.eventplanner41p.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventRepository {

    private final EventDao eventDao;
    private final ExecutorService executorService;
    private final LiveData<List<Event>> upcomingEvents;

    public EventRepository(Application application) {
        EventDatabase database = EventDatabase.getDatabase(application);
        eventDao = database.eventDao();
        executorService = Executors.newSingleThreadExecutor();
        upcomingEvents = eventDao.getUpcomingEvents(System.currentTimeMillis());
    }

    public LiveData<List<Event>> getUpcomingEvents() {
        return upcomingEvents;
    }

    public void insert(Event event) {
        executorService.execute(() -> eventDao.insert(event));
    }

    public void update(Event event) {
        executorService.execute(() -> eventDao.update(event));
    }

    public void delete(Event event) {
        executorService.execute(() -> eventDao.delete(event));
    }
}
package com.example.eventplanner41p.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.eventplanner41p.data.Event;
import com.example.eventplanner41p.data.EventRepository;

import java.util.List;

public class EventViewModel extends AndroidViewModel {

    private final EventRepository repository;
    private final LiveData<List<Event>> upcomingEvents;

    public EventViewModel(@NonNull Application application) {
        super(application);
        repository = new EventRepository(application);
        upcomingEvents = repository.getUpcomingEvents();
    }

    public LiveData<List<Event>> getUpcomingEvents() {
        return upcomingEvents;
    }

    public void insert(Event event) {
        repository.insert(event);
    }

    public void update(Event event) {
        repository.update(event);
    }

    public void delete(Event event) {
        repository.delete(event);
    }
}
package com.example.voyage.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.voyage.database.AppDatabase;
import com.example.voyage.database.dao.EmergencyContactDao;
import com.example.voyage.database.entities.EmergencyContact;
import com.example.voyage.util.SessionManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EmergencyViewModel extends AndroidViewModel {
    private final EmergencyContactDao dao;
    private final SessionManager session;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public EmergencyViewModel(@NonNull Application application) {
        super(application);
        dao = AppDatabase.getInstance(application).emergencyContactDao();
        session = new SessionManager(application);
    }

    public LiveData<List<EmergencyContact>> getContacts() {
        return dao.getContactsForUser(session.getUserId());
    }

    public LiveData<EmergencyContact> getPrimaryContact() {
        return dao.getPrimaryContact(session.getUserId());
    }

    public void insertContact(EmergencyContact contact) {
        executor.execute(() -> dao.insert(contact));
    }

    public void updateContact(EmergencyContact contact) {
        executor.execute(() -> dao.update(contact));
    }

    public void deleteContact(int id) {
        executor.execute(() -> dao.deleteById(id));
    }
}

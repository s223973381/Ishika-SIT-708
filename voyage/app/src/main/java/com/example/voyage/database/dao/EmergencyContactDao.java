package com.example.voyage.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.voyage.database.entities.EmergencyContact;
import java.util.List;

@Dao
public interface EmergencyContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(EmergencyContact contact);

    @Update
    void update(EmergencyContact contact);

    @Delete
    void delete(EmergencyContact contact);

    @Query("SELECT * FROM emergency_contacts WHERE userId = :userId ORDER BY isPrimary DESC, name ASC")
    LiveData<List<EmergencyContact>> getContactsForUser(int userId);

    @Query("SELECT * FROM emergency_contacts WHERE userId = :userId AND isPrimary = 1 LIMIT 1")
    LiveData<EmergencyContact> getPrimaryContact(int userId);

    @Query("DELETE FROM emergency_contacts WHERE contactId = :id")
    void deleteById(int id);
}

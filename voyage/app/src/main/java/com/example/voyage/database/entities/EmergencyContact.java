package com.example.voyage.database.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "emergency_contacts",
    indices = @Index("userId")
)
public class EmergencyContact {
    @PrimaryKey(autoGenerate = true)
    public int contactId;
    public int userId;
    public String name;
    public String phone;
    public String relation;
    public boolean isPrimary;
}

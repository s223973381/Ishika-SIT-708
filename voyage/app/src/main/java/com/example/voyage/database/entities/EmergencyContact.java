package com.example.voyage.database.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "emergency_contacts",
    foreignKeys = @ForeignKey(
        entity = User.class,
        parentColumns = "userId",
        childColumns = "userId",
        onDelete = ForeignKey.CASCADE
    ),
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

package com.example.istreamapp.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "playlist")
public class PlaylistItem {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;    // foreign key to User.id
    public String videoUrl;
}
package com.example.llmchatbot.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ChatMessage.class, User.class}, version = 2, exportSchema = false)
public abstract class ChatDatabase extends RoomDatabase {

    private static volatile ChatDatabase instance;

    public abstract ChatDao chatDao();
    public abstract UserDao userDao();

    public static ChatDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (ChatDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            ChatDatabase.class,
                            "chat_database"
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return instance;
    }
}

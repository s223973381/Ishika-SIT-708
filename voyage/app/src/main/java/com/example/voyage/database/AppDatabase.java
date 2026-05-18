package com.example.voyage.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.voyage.database.dao.*;
import com.example.voyage.database.entities.*;

@Database(
    entities = {
        User.class,
        Trip.class,
        ItineraryItem.class,
        Place.class,
        BudgetExpense.class,
        ChecklistItem.class,
        JournalEntry.class,
        AiChatMessage.class,
        AiCache.class,
        EmergencyContact.class,
        OfflineDownload.class
    },
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract TripDao tripDao();
    public abstract ItineraryItemDao itineraryItemDao();
    public abstract PlaceDao placeDao();
    public abstract BudgetExpenseDao budgetExpenseDao();
    public abstract ChecklistItemDao checklistItemDao();
    public abstract JournalEntryDao journalEntryDao();
    public abstract AiChatMessageDao aiChatMessageDao();
    public abstract AiCacheDao aiCacheDao();
    public abstract EmergencyContactDao emergencyContactDao();
    public abstract OfflineDownloadDao offlineDownloadDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "voyage_db"
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}

package com.example.llmlearningassistant;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "llm_learner.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE = "quiz_history";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "question TEXT, " +
                "user_answer TEXT, " +
                "correct_answer TEXT, " +
                "is_correct INTEGER, " +
                "topic TEXT, " +
                "date_time TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public void insertHistory(String question, String userAnswer, String correctAnswer,
                              boolean isCorrect, String topic) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("question", question);
        cv.put("user_answer", userAnswer);
        cv.put("correct_answer", correctAnswer);
        cv.put("is_correct", isCorrect ? 1 : 0);
        cv.put("topic", topic);
        cv.put("date_time", new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                .format(new Date()));
        db.insert(TABLE, null, cv);
        db.close();
    }

    public List<HistoryModel> getAllHistory() {
        List<HistoryModel> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE + " ORDER BY id DESC", null);
        while (c.moveToNext()) {
            list.add(new HistoryModel(
                    c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("question")),
                    c.getString(c.getColumnIndexOrThrow("user_answer")),
                    c.getString(c.getColumnIndexOrThrow("correct_answer")),
                    c.getInt(c.getColumnIndexOrThrow("is_correct")) == 1,
                    c.getString(c.getColumnIndexOrThrow("topic")),
                    c.getString(c.getColumnIndexOrThrow("date_time"))
            ));
        }
        c.close();
        db.close();
        return list;
    }

    public UserStatsModel getStats() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*), SUM(is_correct) FROM " + TABLE, null);
        int total = 0, correct = 0;
        if (c.moveToFirst()) {
            total = c.getInt(0);
            correct = c.isNull(1) ? 0 : c.getInt(1);
        }
        c.close();
        db.close();
        return new UserStatsModel(total, correct);
    }
}

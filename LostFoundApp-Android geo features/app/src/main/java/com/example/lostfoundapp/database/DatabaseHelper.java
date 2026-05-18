package com.example.lostfoundapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "lost_found.db";
    private static final int DB_VERSION = 2;

    static final String TABLE = "lost_found_items";
    static final String COL_ID = "_id";
    static final String COL_POST_TYPE = "post_type";
    static final String COL_NAME = "name";
    static final String COL_PHONE = "phone";
    static final String COL_DESCRIPTION = "description";
    static final String COL_DATE = "date";
    static final String COL_LOCATION = "location";
    static final String COL_IMAGE_PATH = "image_path";
    static final String COL_CATEGORY = "category";
    static final String COL_TIMESTAMP = "timestamp";
    static final String COL_LATITUDE = "latitude";
    static final String COL_LONGITUDE = "longitude";

    private static final String CREATE_TABLE =
        "CREATE TABLE " + TABLE + " (" +
        COL_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COL_POST_TYPE   + " TEXT NOT NULL, " +
        COL_NAME        + " TEXT NOT NULL, " +
        COL_PHONE       + " TEXT, " +
        COL_DESCRIPTION + " TEXT, " +
        COL_DATE        + " TEXT, " +
        COL_LOCATION    + " TEXT, " +
        COL_IMAGE_PATH  + " TEXT, " +
        COL_CATEGORY    + " TEXT, " +
        COL_TIMESTAMP   + " TEXT, " +
        COL_LATITUDE    + " REAL DEFAULT 0, " +
        COL_LONGITUDE   + " REAL DEFAULT 0)";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE + " ADD COLUMN " + COL_LATITUDE + " REAL DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE + " ADD COLUMN " + COL_LONGITUDE + " REAL DEFAULT 0");
        }
    }

    public long insertItem(LostFoundItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_POST_TYPE,   item.getPostType());
        cv.put(COL_NAME,        item.getName());
        cv.put(COL_PHONE,       item.getPhone());
        cv.put(COL_DESCRIPTION, item.getDescription());
        cv.put(COL_DATE,        item.getDate());
        cv.put(COL_LOCATION,    item.getLocation());
        cv.put(COL_IMAGE_PATH,  item.getImagePath());
        cv.put(COL_CATEGORY,    item.getCategory());
        cv.put(COL_TIMESTAMP,   item.getTimestamp());
        cv.put(COL_LATITUDE,    item.getLatitude());
        cv.put(COL_LONGITUDE,   item.getLongitude());
        long id = db.insert(TABLE, null, cv);
        db.close();
        return id;
    }

    public LostFoundItem getItemById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE, null, COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        LostFoundItem item = null;
        if (cursor.moveToFirst()) item = fromCursor(cursor);
        cursor.close();
        db.close();
        return item;
    }

    public boolean deleteItem(long id) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
    }

    public List<LostFoundItem> searchItems(String keyword, String category) {
        SQLiteDatabase db = getReadableDatabase();
        String selection = null;
        List<String> args = new ArrayList<>();

        boolean hasKeyword = keyword != null && !keyword.isEmpty();
        boolean hasCategory = category != null && !category.equals("All");

        if (hasKeyword && hasCategory) {
            String like = "%" + keyword + "%";
            selection = "(" + COL_NAME + " LIKE ? OR " + COL_DESCRIPTION + " LIKE ? OR "
                    + COL_LOCATION + " LIKE ?) AND " + COL_CATEGORY + "=?";
            args.add(like); args.add(like); args.add(like); args.add(category);
        } else if (hasKeyword) {
            String like = "%" + keyword + "%";
            selection = COL_NAME + " LIKE ? OR " + COL_DESCRIPTION + " LIKE ? OR "
                    + COL_LOCATION + " LIKE ?";
            args.add(like); args.add(like); args.add(like);
        } else if (hasCategory) {
            selection = COL_CATEGORY + "=?";
            args.add(category);
        }

        Cursor cursor = db.query(TABLE, null, selection,
                args.isEmpty() ? null : args.toArray(new String[0]),
                null, null, COL_TIMESTAMP + " DESC");
        List<LostFoundItem> items = listFromCursor(cursor);
        cursor.close();
        db.close();
        return items;
    }

    public List<LostFoundItem> getAllItems() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE, null, null, null, null, null, COL_TIMESTAMP + " DESC");
        List<LostFoundItem> items = listFromCursor(cursor);
        cursor.close();
        db.close();
        return items;
    }

    private List<LostFoundItem> listFromCursor(Cursor cursor) {
        List<LostFoundItem> items = new ArrayList<>();
        while (cursor.moveToNext()) items.add(fromCursor(cursor));
        return items;
    }

    private LostFoundItem fromCursor(Cursor c) {
        LostFoundItem item = new LostFoundItem();
        item.setId(c.getLong(c.getColumnIndexOrThrow(COL_ID)));
        item.setPostType(c.getString(c.getColumnIndexOrThrow(COL_POST_TYPE)));
        item.setName(c.getString(c.getColumnIndexOrThrow(COL_NAME)));
        item.setPhone(c.getString(c.getColumnIndexOrThrow(COL_PHONE)));
        item.setDescription(c.getString(c.getColumnIndexOrThrow(COL_DESCRIPTION)));
        item.setDate(c.getString(c.getColumnIndexOrThrow(COL_DATE)));
        item.setLocation(c.getString(c.getColumnIndexOrThrow(COL_LOCATION)));
        item.setImagePath(c.getString(c.getColumnIndexOrThrow(COL_IMAGE_PATH)));
        item.setCategory(c.getString(c.getColumnIndexOrThrow(COL_CATEGORY)));
        item.setTimestamp(c.getString(c.getColumnIndexOrThrow(COL_TIMESTAMP)));
        int latIdx = c.getColumnIndex(COL_LATITUDE);
        int lngIdx = c.getColumnIndex(COL_LONGITUDE);
        if (latIdx != -1) item.setLatitude(c.getDouble(latIdx));
        if (lngIdx != -1) item.setLongitude(c.getDouble(lngIdx));
        return item;
    }
}

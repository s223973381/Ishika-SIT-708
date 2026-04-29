package com.example.sportsnewsfeedapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class BookmarkManager {

    private static final String PREF_NAME = "bookmarks_pref";
    private static final String KEY_BOOKMARKS = "bookmarked_ids";

    public static Set<String> getBookmarks(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return new HashSet<>(prefs.getStringSet(KEY_BOOKMARKS, new HashSet<>()));
    }

    public static void toggleBookmark(Context context, int newsId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> bookmarks = getBookmarks(context);
        String id = String.valueOf(newsId);

        if (bookmarks.contains(id)) {
            bookmarks.remove(id);
        } else {
            bookmarks.add(id);
        }

        prefs.edit().putStringSet(KEY_BOOKMARKS, bookmarks).apply();
    }

    public static boolean isBookmarked(Context context, int newsId) {
        return getBookmarks(context).contains(String.valueOf(newsId));
    }
}
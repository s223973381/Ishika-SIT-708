package com.example.lostfoundapp.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateTimeUtils {

    private static final String DATE_FORMAT = "dd MMM yyyy";
    private static final String DATETIME_FORMAT = "dd MMM yyyy, hh:mm a";

    public static String getCurrentDate() {
        return new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(new Date());
    }

    public static String getCurrentTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }

    public static String getRelativeTime(String timestampStr) {
        try {
            long ts = Long.parseLong(timestampStr);
            long diff = System.currentTimeMillis() - ts;
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            long hours   = TimeUnit.MILLISECONDS.toHours(diff);
            long days    = TimeUnit.MILLISECONDS.toDays(diff);

            if (minutes < 1)  return "Just now";
            if (minutes < 60) return minutes + " min ago";
            if (hours < 24)   return hours + (hours == 1 ? " hour ago" : " hours ago");
            if (days < 7)     return days + (days == 1 ? " day ago" : " days ago");
            if (days < 30)    return (days / 7) + ((days / 7) == 1 ? " week ago" : " weeks ago");
            return new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(new Date(ts));
        } catch (NumberFormatException e) {
            return timestampStr;
        }
    }

    public static String formatTimestamp(String timestampStr) {
        try {
            long ts = Long.parseLong(timestampStr);
            return new SimpleDateFormat(DATETIME_FORMAT, Locale.getDefault()).format(new Date(ts));
        } catch (NumberFormatException e) {
            return timestampStr;
        }
    }
}

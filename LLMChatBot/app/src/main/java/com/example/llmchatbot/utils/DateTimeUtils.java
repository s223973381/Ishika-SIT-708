package com.example.llmchatbot.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {

    private static final SimpleDateFormat TIME_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final SimpleDateFormat DATE_TIME_FORMAT =
            new SimpleDateFormat("MMM d, HH:mm", Locale.getDefault());

    public static String formatTime(long timestampMs) {
        return TIME_FORMAT.format(new Date(timestampMs));
    }

    public static String formatDateTime(long timestampMs) {
        return DATE_TIME_FORMAT.format(new Date(timestampMs));
    }

    public static long now() {
        return System.currentTimeMillis();
    }
}

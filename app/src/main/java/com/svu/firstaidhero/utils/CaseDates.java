package com.svu.firstaidhero.utils;

import android.content.Context;

import com.svu.firstaidhero.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Formats and parses the date string stored on a treated case. */
public final class CaseDates {

    private static final String PATTERN = "yyyy/MM/dd - HH:mm";

    private CaseDates() {
        // Utility class
    }

    public static String now() {
        return new SimpleDateFormat(PATTERN, Locale.US).format(new Date());
    }

    /** Short "time ago" text such as "قبل 2 س", or null if the stored date can't be read. */
    public static String timeAgo(Context context, String stored) {
        Date date;
        try {
            date = new SimpleDateFormat(PATTERN, Locale.US).parse(stored);
        } catch (ParseException e) {
            return null;
        }
        if (date == null) return null;

        long minutes = (System.currentTimeMillis() - date.getTime()) / 60000;
        if (minutes < 1) return context.getString(R.string.time_just_now);
        if (minutes < 60) return context.getString(R.string.time_minutes, (int) minutes);
        long hours = minutes / 60;
        if (hours < 24) return context.getString(R.string.time_hours, (int) hours);
        return context.getString(R.string.time_days, (int) (hours / 24));
    }
}

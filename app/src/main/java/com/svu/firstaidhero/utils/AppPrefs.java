package com.svu.firstaidhero.utils;

import android.content.Context;
import android.content.SharedPreferences;

/** The user's settings from the Settings tab. */
public final class AppPrefs {

    private static final String FILE = "nabd_prefs";
    private static final String KEY_KEEP_SCREEN_ON = "keep_screen_on";
    private static final String KEY_LARGE_TEXT = "large_step_text";

    private AppPrefs() {
        // Utility class
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    /** Keep the screen awake while a protocol is open (on by default: hands are busy). */
    public static boolean keepScreenOn(Context context) {
        return prefs(context).getBoolean(KEY_KEEP_SCREEN_ON, true);
    }

    public static void setKeepScreenOn(Context context, boolean value) {
        prefs(context).edit().putBoolean(KEY_KEEP_SCREEN_ON, value).apply();
    }

    public static boolean largeStepText(Context context) {
        return prefs(context).getBoolean(KEY_LARGE_TEXT, false);
    }

    public static void setLargeStepText(Context context, boolean value) {
        prefs(context).edit().putBoolean(KEY_LARGE_TEXT, value).apply();
    }
}

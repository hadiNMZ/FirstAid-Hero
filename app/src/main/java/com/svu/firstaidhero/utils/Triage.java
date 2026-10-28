package com.svu.firstaidhero.utils;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.svu.firstaidhero.R;

/** Triage levels stored on a treated case, with their labels and colours. */
public final class Triage {

    public static final String STABLE = "stable";
    public static final String URGENT = "urgent";
    public static final String CRITICAL = "critical";

    private Triage() {
        // Utility class
    }

    @StringRes
    public static int label(String level) {
        if (CRITICAL.equals(level)) return R.string.triage_critical;
        if (URGENT.equals(level)) return R.string.triage_urgent;
        return R.string.triage_stable;
    }

    @StringRes
    public static int shortLabel(String level) {
        if (CRITICAL.equals(level)) return R.string.triage_short_critical;
        if (URGENT.equals(level)) return R.string.triage_short_urgent;
        return R.string.triage_short_stable;
    }

    @ColorRes
    public static int color(String level) {
        if (CRITICAL.equals(level)) return R.color.nabd_emergency;
        if (URGENT.equals(level)) return R.color.nabd_amber;
        return R.color.nabd_green;
    }

    @DrawableRes
    public static int pill(String level) {
        if (CRITICAL.equals(level)) return R.drawable.bg_pill_pink;
        if (URGENT.equals(level)) return R.drawable.bg_pill_amber;
        return R.drawable.bg_pill_green;
    }
}

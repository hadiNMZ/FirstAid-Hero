package com.svu.firstaidhero.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

/**
 * Looks up optional photos by name, so images exported from the design can be added
 * to res/drawable-nodpi (e.g. kit_tourniquet.jpg, hero_bleeding.jpg) without code changes.
 */
public final class ImageResources {

    private ImageResources() {
        // Utility class
    }

    /** Returns the drawable id for {@code name}, or 0 when no such image has been added. */
    @DrawableRes
    @SuppressLint("DiscouragedApi")
    public static int find(Context context, @Nullable String name) {
        if (name == null || name.isEmpty()) return 0;
        return context.getResources().getIdentifier(name, "drawable", context.getPackageName());
    }

    /** Converts Western digits to Arabic-Indic ones, as the design numbers its steps (١، ٢، ٣). */
    public static String arabicDigits(int number) {
        String digits = String.valueOf(number);
        StringBuilder sb = new StringBuilder(digits.length());
        for (char c : digits.toCharArray()) {
            sb.append(Character.isDigit(c) ? (char) ('٠' + (c - '0')) : c);
        }
        return sb.toString();
    }
}

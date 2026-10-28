package com.svu.firstaidhero.utils;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.svu.firstaidhero.R;
import com.svu.firstaidhero.database.entities.KitItem;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Works out whether a kit item is complete, low, expiring soon or expired. */
public enum KitStatus {
    FULL(R.string.kit_status_full, R.color.nabd_green, R.drawable.bg_pill_green),
    EXPIRING(R.string.kit_status_expiring, R.color.nabd_amber, R.drawable.bg_pill_amber),
    LOW(R.string.kit_status_low, R.color.nabd_emergency, R.drawable.bg_pill_pink),
    EXPIRED(R.string.kit_status_expired, R.color.nabd_emergency, R.drawable.bg_pill_pink);

    private static final int EXPIRING_MONTHS = 3;

    @StringRes public final int label;
    @ColorRes public final int color;
    @DrawableRes public final int pill;

    KitStatus(int label, int color, int pill) {
        this.label = label;
        this.color = color;
        this.pill = pill;
    }

    /** True when the item needs restocking or replacing. */
    public boolean isProblem() {
        return this == LOW || this == EXPIRED;
    }

    public static KitStatus of(KitItem item) {
        String expiry = item.getExpiry();
        // Expiry is stored as "yyyy/MM", so plain string comparison orders it correctly
        if (expiry != null && expiry.compareTo(monthFromNow(0)) < 0) return EXPIRED;
        if (item.getQuantity() < item.getRequiredQuantity()) return LOW;
        if (expiry != null && expiry.compareTo(monthFromNow(EXPIRING_MONTHS)) <= 0) return EXPIRING;
        return FULL;
    }

    private static String monthFromNow(int months) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, months);
        Date date = calendar.getTime();
        return new SimpleDateFormat("yyyy/MM", Locale.US).format(date);
    }
}

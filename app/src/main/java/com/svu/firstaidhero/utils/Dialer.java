package com.svu.firstaidhero.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.svu.firstaidhero.R;

/** Opens the phone dialer or messaging app with a number filled in; the user confirms. */
public final class Dialer {

    public static final String EMERGENCY_NUMBER = "110";

    private Dialer() {
        // Utility class
    }

    public static void dial(Context context, String number) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.dialer_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    /** Opens the messaging app with a new SMS addressed to the number. */
    public static void sendSms(Context context, String number) {
        sendSms(context, number, null);
    }

    /** Opens the messaging app with an SMS to the number, pre-filled with {@code body} if given. */
    public static void sendSms(Context context, String number, String body) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + number));
        if (body != null) intent.putExtra("sms_body", body);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.sms_not_found, Toast.LENGTH_SHORT).show();
        }
    }
}

package com.svu.firstaidhero.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.svu.firstaidhero.database.entities.CustomContact;

import java.util.List;
import java.util.Locale;

/**
 * Panic-Proof Emergency SOS SMS Location Dispatcher.
 * Directly queries device's latest recorded location (works even when GPS is turned off),
 * formats compact Arabic coordinates payload, and dispatches via SMS.
 */
public class SosDispatcher {

    public static final int REQUEST_LOCATION_PERMISSION = 2001;

    private static final long GPS_TIMEOUT_MS = 5000; // 5 seconds strict timeout

    private static final String FALLBACK_NO_PERMISSION =
            "نداء استغاثة طارئ! (تعذر تحديد موقع GPS تلقائياً - يرجى الاتصال بي فوراً)";

    private static final String FALLBACK_TIMEOUT_OR_NULL =
            "نداء استغاثة طارئ! (تعذر التقاط الإحداثيات - يرجى الاتصال بي فوراً)";

    public interface LocationCallback {
        void onMessageReady(String messageText);
    }

    /**
     * Main panic-proof entry point to prepare emergency SOS SMS payload.
     */
    public static void prepareSosMessage(Activity activity, LocationCallback callback) {
        if (activity == null || callback == null) return;

        boolean hasFine = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean hasCoarse = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (!hasFine && !hasCoarse) {
            // Permission NOT GRANTED: Request permission and callback location-less fallback payload
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);

            callback.onMessageReady(FALLBACK_NO_PERMISSION);
            return;
        }

        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            callback.onMessageReady(FALLBACK_TIMEOUT_OR_NULL);
            return;
        }

        // a. Query latest recorded location directly from device (works even when GPS is turned off!)
        Location bestLocation = null;
        try {
            Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location netLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location passiveLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            bestLocation = getMostRecentLocation(gpsLocation, netLocation, passiveLocation);
        } catch (SecurityException ignored) { }

        // b. If a recorded location exists on the device, format and send it IMMEDIATELY with zero delay!
        if (bestLocation != null) {
            callback.onMessageReady(formatLocationMessage(bestLocation));
            return;
        }

        // c. If NO cached location exists, check if live GPS is enabled
        boolean isGpsEnabled = false;
        boolean isNetworkEnabled = false;
        try {
            isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ignored) { }

        if (!isGpsEnabled && !isNetworkEnabled) {
            // Devices with no recorded location history and disabled GPS return fallback notice
            callback.onMessageReady(FALLBACK_TIMEOUT_OR_NULL);
            return;
        }

        // d. Request a single live location update backed by 5000ms strict timeout
        Handler handler = new Handler(Looper.getMainLooper());
        final boolean[] isDispatched = {false};

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (isDispatched[0]) return;
                isDispatched[0] = true;

                handler.removeCallbacksAndMessages(null);
                try {
                    locationManager.removeUpdates(this);
                } catch (SecurityException ignored) { }

                if (location != null) {
                    callback.onMessageReady(formatLocationMessage(location));
                } else {
                    callback.onMessageReady(FALLBACK_TIMEOUT_OR_NULL);
                }
            }

            @Override public void onStatusChanged(String provider, int status, Bundle extras) { }
            @Override public void onProviderEnabled(String provider) { }
            @Override public void onProviderDisabled(String provider) { }
        };

        // Strict 5-second timeout
        Runnable timeoutRunnable = () -> {
            if (isDispatched[0]) return;
            isDispatched[0] = true;

            try {
                locationManager.removeUpdates(locationListener);
            } catch (SecurityException ignored) { }

            callback.onMessageReady(FALLBACK_TIMEOUT_OR_NULL);
        };

        handler.postDelayed(timeoutRunnable, GPS_TIMEOUT_MS);

        try {
            if (isGpsEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener, Looper.getMainLooper());
            } else {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener, Looper.getMainLooper());
            }
        } catch (SecurityException e) {
            handler.removeCallbacks(timeoutRunnable);
            isDispatched[0] = true;
            callback.onMessageReady(FALLBACK_NO_PERMISSION);
        }
    }

    private static Location getMostRecentLocation(Location locA, Location locB, Location locC) {
        Location best = null;
        if (locA != null) best = locA;
        if (locB != null && (best == null || locB.getTime() > best.getTime())) best = locB;
        if (locC != null && (best == null || locC.getTime() > best.getTime())) best = locC;
        return best;
    }

    /**
     * Formats compact Arabic SOS payload using Locale.US (5 decimal places).
     */
    public static String formatLocationMessage(Location location) {
        if (location == null) return FALLBACK_TIMEOUT_OR_NULL;

        double lat = location.getLatitude();
        double lng = location.getLongitude();

        return String.format(Locale.US,
                "نداء استغاثة طارئ!\nالموقع: %1$.5f, %2$.5f\nhttps://maps.google.com/?q=%1$.5f,%2$.5f",
                lat, lng);
    }

    /**
     * Dispatches the prepared SOS message to a single person's phone number.
     */
    public static void dispatchSmsToSinglePerson(Context context, String phoneNumber, String message) {
        if (context == null || TextUtils.isEmpty(message)) return;

        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        if (!TextUtils.isEmpty(phoneNumber)) {
            smsIntent.setData(Uri.parse("smsto:" + phoneNumber.trim()));
        } else {
            smsIntent.setData(Uri.parse("smsto:"));
        }
        smsIntent.putExtra("sms_body", message);

        try {
            context.startActivity(smsIntent);
        } catch (Exception e) {
            Toast.makeText(context, "تعذر فتح تطبيق الرسائل النصية", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Dispatches the prepared SOS message to a list of saved contacts.
     */
    public static void dispatchSms(Context context, List<CustomContact> contacts, String message) {
        if (context == null || TextUtils.isEmpty(message)) return;

        StringBuilder recipients = new StringBuilder();
        if (contacts != null && !contacts.isEmpty()) {
            for (CustomContact c : contacts) {
                if (c != null && !TextUtils.isEmpty(c.getPhoneNumber())) {
                    if (recipients.length() > 0) recipients.append(";");
                    recipients.append(c.getPhoneNumber());
                }
            }
        }

        dispatchSmsToSinglePerson(context, recipients.toString(), message);
    }
}

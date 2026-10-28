package com.svu.firstaidhero.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;

/** Decodes a photo downscaled to roughly the size it is shown at, so camera images don't exhaust memory. */
public final class ImageLoader {

    private ImageLoader() {
        // Utility class
    }

    /** Returns the image at {@code uri} with its longest side near {@code targetPx}, or null if unreadable. */
    @Nullable
    public static Bitmap load(Context context, Uri uri, int targetPx) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            try (InputStream in = context.getContentResolver().openInputStream(uri)) {
                BitmapFactory.decodeStream(in, null, bounds);
            }

            int sample = 1;
            int longest = Math.max(bounds.outWidth, bounds.outHeight);
            while (longest / (sample * 2) >= targetPx) {
                sample *= 2;
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sample;
            try (InputStream in = context.getContentResolver().openInputStream(uri)) {
                return BitmapFactory.decodeStream(in, null, options);
            }
        } catch (IOException | SecurityException e) {
            // Photo was deleted or access was revoked; show the card without it
            return null;
        }
    }
}

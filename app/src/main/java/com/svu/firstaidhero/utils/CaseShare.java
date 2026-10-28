package com.svu.firstaidhero.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.svu.firstaidhero.R;
import com.svu.firstaidhero.database.entities.TreatedCase;

import java.util.List;

/** Builds the plain-text report for treated cases and opens the share sheet. */
public final class CaseShare {

    private CaseShare() {
        // Utility class
    }

    public static String reportText(Context context, TreatedCase c) {
        return context.getString(R.string.share_case_text,
                c.getDescription(),
                c.getDateTreated(),
                context.getString(Triage.label(c.getTriageLevel())),
                c.getProceduralNotes());
    }

    /** Shares one case as text, attaching its photo when there is one. */
    public static void share(Context context, TreatedCase c) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, reportText(context, c));
        String photo = c.getPhotoUri();
        if (photo != null && !photo.isEmpty()) {
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(photo));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent.setType("text/plain");
        }
        open(context, intent);
    }

    /** Shares the whole log as one text message. */
    public static void shareAll(Context context, List<TreatedCase> cases) {
        StringBuilder sb = new StringBuilder();
        for (TreatedCase c : cases) {
            if (sb.length() > 0) sb.append("\n\n────────\n\n");
            sb.append(reportText(context, c));
        }
        shareText(context, sb.toString());
    }

    public static void shareText(Context context, String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        open(context, intent);
    }

    private static void open(Context context, Intent intent) {
        try {
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_via)));
        } catch (ActivityNotFoundException ignored) {
            // No app can share; nothing else to do
        }
    }
}

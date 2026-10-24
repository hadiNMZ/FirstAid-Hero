package com.svu.firstaidhero.utils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cleans raw text coming from the pre-populated database before it is shown in the UI.
 * Pure Java (no android.* or org.json) so it can be unit-tested on the JVM.
 */
public final class TextSanitizer {

    private static final String BULLET = "• ";
    private static final Pattern QUOTED_ITEM = Pattern.compile("\"((?:[^\"\\\\]|\\\\.)*)\"");

    private TextSanitizer() {
        // Utility class
    }

    /**
     * Returns a clean title. If the raw value is an array like ["سؤال؟", "حروق"],
     * the last non-empty item is used.
     */
    public static String cleanTitle(String rawTitle) {
        if (rawTitle == null) return "";
        String text = rawTitle.trim();
        if (text.isEmpty()) return "";

        if (text.startsWith("[")) {
            List<String> items = parseArrayItems(text);
            for (int i = items.size() - 1; i >= 0; i--) {
                if (!items.get(i).isEmpty()) return items.get(i);
            }
            return "";
        }
        return stripQuotes(text);
    }

    /**
     * Returns symptoms as a bullet list, one symptom per line:
     * • عرض 1
     * • عرض 2
     */
    public static String cleanSymptoms(String rawSymptoms) {
        if (rawSymptoms == null) return "";
        String text = rawSymptoms.trim();
        if (text.isEmpty()) return "";

        List<String> parts = new ArrayList<>();
        if (text.startsWith("[")) {
            for (String item : parseArrayItems(text)) {
                splitInto(item, parts);
            }
        } else {
            splitInto(text, parts);
        }

        // Remove empty entries and duplicates while keeping order
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String p : parts) {
            String t = stripQuotes(p.trim());
            if (!t.isEmpty()) unique.add(t);
        }

        StringBuilder sb = new StringBuilder();
        for (String s : unique) {
            if (sb.length() > 0) sb.append('\n');
            sb.append(BULLET).append(s);
        }
        return sb.toString();
    }

    /** Maps the database danger level to its Arabic badge text. */
    public static String getArabicDangerLevel(String dangerLevel) {
        if ("High".equalsIgnoreCase(dangerLevel)) return "حالة حرجة";
        if ("Minor".equalsIgnoreCase(dangerLevel)) return "إصابة بسيطة";
        return "غير محدد";
    }

    // ---------------------------------------------------------------------

    /** Extracts items from a JSON-like array string; falls back to comma splitting. */
    private static List<String> parseArrayItems(String text) {
        List<String> items = new ArrayList<>();
        Matcher m = QUOTED_ITEM.matcher(text);
        while (m.find()) {
            items.add(m.group(1).replace("\\\"", "\"").replace("\\\\", "\\").trim());
        }
        if (!items.isEmpty()) return items;

        // No quotes found, e.g. [حروق, كيف أعالج الحروق؟]
        String inner = text.replaceAll("^\\[|\\]$", "");
        for (String part : inner.split("[,،]")) {
            items.add(stripQuotes(part.trim()));
        }
        return items;
    }

    /** Splits on pipe, English comma and Arabic comma, and also on newlines. */
    private static void splitInto(String text, List<String> out) {
        for (String part : text.split("[|,،\\n]")) {
            out.add(part);
        }
    }

    /** Removes leftover brackets and quote characters around a value. */
    private static String stripQuotes(String s) {
        return s.replaceAll("^[\\[\\]\"'“”]+|[\\[\\]\"'“”]+$", "").trim();
    }
}

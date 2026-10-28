package com.svu.firstaidhero.utils;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.svu.firstaidhero.R;
import com.svu.firstaidhero.database.entities.Emergency;

import java.util.Locale;

/**
 * Groups an emergency into a broad category (by its title and keywords) so it can show
 * the matching icon, label and optional hero photo from the design.
 */
public enum EmergencyCategory {
    BLEEDING("bleeding", R.string.cat_bleeding, R.drawable.ic_emergency,
            "نزيف", "نزف", "رعاف", "جرح", "جروح", "بتر", "bleed", "wound", "cut"),
    BURNS("burns", R.string.cat_burns, R.drawable.ic_fire,
            "حرق", "حروق", "burn"),
    BREATHING("breathing", R.string.cat_breathing, R.drawable.ic_air,
            "اختناق", "يختنق", "تنفس", "الربو", "غرق", "يغرق", "choking", "drown", "asthma"),
    HEART("heart", R.string.cat_heart, R.drawable.ic_heart,
            "قلب", "قلبي", "القلبية", "إنعاش", "سكتة", "cpr", "heart"),
    BONES("bones", R.string.cat_bones, R.drawable.ic_healing,
            "كسر", "مكسور", "التواء", "خلع", "شد العضل", "fracture", "sprain", "strain", "broken"),
    BITES("bites", R.string.cat_bites, R.drawable.ic_warning,
            "لدغ", "لسع", "عض", "تسمم", "سام", "أفعى", "bite", "sting", "poison"),
    WEATHER("weather", R.string.cat_weather, R.drawable.ic_sunny,
            "حرار", "الشمس", "صقيع", "البرد", "البارد", "heat", "frost", "sun"),
    GENERAL("general", R.string.cat_general, R.drawable.ic_health);

    public final String key;
    @StringRes public final int label;
    @DrawableRes public final int icon;
    private final String[] words;

    EmergencyCategory(String key, int label, int icon, String... words) {
        this.key = key;
        this.label = label;
        this.icon = icon;
        this.words = words;
    }

    /** Drawable name of the optional hero photo for this category, e.g. hero_bleeding. */
    public String heroImageName() {
        return "hero_" + key;
    }

    public static EmergencyCategory of(Emergency emergency) {
        String text = (emergency.getTitle() + " " + emergency.getKeywords()).toLowerCase(Locale.ROOT);
        for (EmergencyCategory category : values()) {
            for (String word : category.words) {
                if (text.contains(word)) return category;
            }
        }
        return GENERAL;
    }
}

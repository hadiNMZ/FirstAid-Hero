package com.svu.firstaidhero.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.svu.firstaidhero.database.dao.CustomContactDao;
import com.svu.firstaidhero.database.dao.EmergencyDao;
import com.svu.firstaidhero.database.dao.KitItemDao;
import com.svu.firstaidhero.database.dao.StepDao;
import com.svu.firstaidhero.database.dao.TreatedCaseDao;
import com.svu.firstaidhero.database.entities.CustomContact;
import com.svu.firstaidhero.database.entities.Emergency;
import com.svu.firstaidhero.database.entities.KitItem;
import com.svu.firstaidhero.database.entities.Step;
import com.svu.firstaidhero.database.entities.TreatedCase;

@Database(entities = {Emergency.class, Step.class, CustomContact.class, TreatedCase.class, KitItem.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "firstaid_hero.db";
    private static volatile AppDatabase INSTANCE;

    public abstract EmergencyDao emergencyDao();
    public abstract StepDao stepDao();
    public abstract CustomContactDao customContactDao();
    public abstract TreatedCaseDao treatedCaseDao();
    public abstract KitItemDao kitItemDao();

    /**
     * Version 2: triage level on treated cases, and the first-aid kit inventory.
     * Also runs on fresh installs, because the bundled asset database is version 1.
     */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE `treated_cases` ADD COLUMN `triage_level` TEXT NOT NULL DEFAULT 'stable'");

            // Earlier builds stored the triage level as the first line of the notes; move it to the column
            moveTriageOutOfNotes(db, "حرجة", "critical");
            moveTriageOutOfNotes(db, "عاجلة", "urgent");
            moveTriageOutOfNotes(db, "مستقرة", "stable");

            db.execSQL("CREATE TABLE IF NOT EXISTS `kit_items` ("
                    + "`item_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "`name` TEXT NOT NULL, `name_en` TEXT NOT NULL, `category` TEXT NOT NULL, "
                    + "`quantity` INTEGER NOT NULL, `required_quantity` INTEGER NOT NULL, "
                    + "`unit` TEXT NOT NULL, `expiry` TEXT, `image_name` TEXT)");

            // A standard starter kit; the user adjusts quantities from the Kit tab
            Object[][] items = {
                    {"عاصبة طبية معتمدة (CAT)", "Combat Application Tourniquet", KitItem.CATEGORY_BLEEDING, 2, 2, "قطعة", null, "kit_tourniquet"},
                    {"شاش مرقئ ومضغوط للنزيف", "Hemostatic Gauze Z-Fold", KitItem.CATEGORY_BLEEDING, 3, 3, "لفات", "2027/11", "kit_hemostatic_gauze"},
                    {"ضمادة ضاغطة للطوارئ", "Emergency Compression Bandage", KitItem.CATEGORY_BLEEDING, 2, 2, "وحدة", "2028/04", "kit_compression_bandage"},
                    {"كمادات حروق هيدروجيل مبردة", "Burn Gel Dressing 10x10 cm", KitItem.CATEGORY_BURNS, 2, 2, "كمادة", "2027/09", "kit_burn_gel"},
                    {"لفائف شاش ومطهر بوفيدون", "Gauze Rolls & Povidone-Iodine", KitItem.CATEGORY_BURNS, 1, 4, "لفة", "2027/06", "kit_gauze_rolls"},
                    {"محلول غسيل العين المعقم", "Sterile Eye Wash Solution", KitItem.CATEGORY_BURNS, 2, 2, "عبوة", "2027/02", "kit_eye_wash"},
                    {"قفازات نيتريل طبية", "Nitrile Examination Gloves", KitItem.CATEGORY_TOOLS, 6, 10, "زوج", null, "kit_gloves"},
                    {"مقص ضمادات طبي", "Trauma Shears", KitItem.CATEGORY_TOOLS, 1, 1, "قطعة", null, "kit_shears"},
                    {"قناع إنعاش CPR", "CPR Pocket Mask", KitItem.CATEGORY_TOOLS, 1, 1, "قطعة", null, "kit_cpr_mask"},
                    {"بطانية حرارية للطوارئ", "Emergency Thermal Blanket", KitItem.CATEGORY_TOOLS, 2, 2, "قطعة", null, "kit_thermal_blanket"},
            };
            for (Object[] item : items) {
                db.execSQL("INSERT INTO `kit_items` (`name`, `name_en`, `category`, `quantity`, "
                        + "`required_quantity`, `unit`, `expiry`, `image_name`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", item);
            }
        }

        private void moveTriageOutOfNotes(SupportSQLiteDatabase db, String label, String level) {
            db.execSQL("UPDATE `treated_cases` SET `triage_level` = ?, "
                            + "`procedural_notes` = substr(`procedural_notes`, instr(`procedural_notes`, char(10)) + 1) "
                            + "WHERE `procedural_notes` LIKE ?",
                    new Object[]{level, "درجة الفرز: " + label + "%"});
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    )
                    .createFromAsset("databases/" + DATABASE_NAME)
                    .addMigrations(MIGRATION_1_2)
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}

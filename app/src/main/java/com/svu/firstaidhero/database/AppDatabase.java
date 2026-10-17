package com.svu.firstaidhero.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.svu.firstaidhero.database.dao.CustomContactDao;
import com.svu.firstaidhero.database.dao.EmergencyDao;
import com.svu.firstaidhero.database.dao.StepDao;
import com.svu.firstaidhero.database.dao.TreatedCaseDao;
import com.svu.firstaidhero.database.entities.CustomContact;
import com.svu.firstaidhero.database.entities.Emergency;
import com.svu.firstaidhero.database.entities.Step;
import com.svu.firstaidhero.database.entities.TreatedCase;

@Database(entities = {Emergency.class, Step.class, CustomContact.class, TreatedCase.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "firstaid_hero.db";
    private static volatile AppDatabase INSTANCE;

    public abstract EmergencyDao emergencyDao();
    public abstract StepDao stepDao();
    public abstract CustomContactDao customContactDao();
    public abstract TreatedCaseDao treatedCaseDao();

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
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}

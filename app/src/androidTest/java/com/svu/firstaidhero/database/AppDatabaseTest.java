package com.svu.firstaidhero.database;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.svu.firstaidhero.database.dao.EmergencyDao;
import com.svu.firstaidhero.database.dao.StepDao;
import com.svu.firstaidhero.database.entities.Emergency;
import com.svu.firstaidhero.database.entities.Step;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AppDatabaseTest {

    private AppDatabase db;
    private EmergencyDao emergencyDao;
    private StepDao stepDao;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.databaseBuilder(context, AppDatabase.class, "test_firstaid_hero.db")
                .createFromAsset("databases/firstaid_hero.db")
                .allowMainThreadQueries()
                .build();

        emergencyDao = db.emergencyDao();
        stepDao = db.stepDao();
    }

    @After
    public void tearDown() {
        if (db != null) {
            db.close();
        }
    }

    @Test
    public void testDatabaseIsPrePopulated() {
        List<Emergency> emergencies = emergencyDao.getAllEmergencies();
        assertNotNull("Emergency list should not be null", emergencies);
        assertTrue("Emergency list size should be greater than 0", emergencies.size() > 0);
    }

    @Test
    public void testArabicSearchQuery() {
        List<Emergency> searchResults = emergencyDao.searchEmergencies("حروق");
        assertNotNull("Search results list should not be null", searchResults);
        assertFalse("Search results for 'حروق' should not be empty", searchResults.isEmpty());
    }

    @Test
    public void testStepsAreLoaded() {
        List<Step> steps = stepDao.getStepsForEmergency(1);
        assertNotNull("Steps list should not be null", steps);
        assertFalse("Steps list for case_id = 1 should not be empty", steps.isEmpty());
    }
}

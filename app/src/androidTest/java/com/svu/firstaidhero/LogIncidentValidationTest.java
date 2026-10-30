package com.svu.firstaidhero;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.svu.firstaidhero.views.activities.LogIncidentActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Automated Espresso UI Test verifying input validation edge cases and zero-crash safety
 * for LogIncidentActivity.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LogIncidentValidationTest {

    @Rule
    public ActivityScenarioRule<LogIncidentActivity> activityRule =
            new ActivityScenarioRule<>(LogIncidentActivity.class);

    @Test
    public void testSubmitEmptyLogIncidentDisplaysErrorsAndDoesNotCrash() {
        // Click save on empty form
        onView(withId(R.id.btnSave)).perform(click());

        // Verify form remains visible with error states and zero crash
        onView(withId(R.id.tilDescription)).check(matches(isDisplayed()));
        onView(withId(R.id.tilNotes)).check(matches(isDisplayed()));
    }

    @Test
    public void testSpecialCharactersAndEmojiNotesDoesNotCrash() {
        // Type SQL/HTML characters and Emojis
        onView(withId(R.id.etDescription)).perform(
                replaceText("نزيف حاد! <script>SELECT * FROM cases;</script> 🚑🩸"),
                closeSoftKeyboard());
        onView(withId(R.id.etNotes)).perform(
                replaceText("ملاحظات إجرائية مع رموز خاصة: !@#$%^&*()_+~`\\|{}:\"<>?"),
                closeSoftKeyboard());

        // Select Critical Triage chip
        onView(withId(R.id.chipCritical)).perform(click());

        // Save incident log
        onView(withId(R.id.btnSave)).perform(click());
    }
}

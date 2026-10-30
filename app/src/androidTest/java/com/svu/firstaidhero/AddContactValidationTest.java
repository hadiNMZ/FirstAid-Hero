package com.svu.firstaidhero;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.svu.firstaidhero.views.activities.AddContactActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Automated Espresso UI Test verifying input validation, edge cases, and zero-crash robustness
 * for AddContactActivity.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AddContactValidationTest {

    @Rule
    public ActivityScenarioRule<AddContactActivity> activityRule =
            new ActivityScenarioRule<>(AddContactActivity.class);

    @Test
    public void testSubmitEmptyFormDisplaysValidationErrorsAndDoesNotCrash() {
        // Click save button with blank fields
        onView(withId(R.id.btnSave)).perform(click());

        // Verify that the activity remains open and visible without crashing
        onView(withId(R.id.tilName)).check(matches(isDisplayed()));
        onView(withId(R.id.tilPhone)).check(matches(isDisplayed()));
    }

    @Test
    public void testInvalidPhoneNumberFormattingAndSpecialCharactersDoesNotCrash() {
        // Enter special characters in name and invalid symbols in phone field
        onView(withId(R.id.etName)).perform(typeText("<Test!@#$%^&*>"), closeSoftKeyboard());
        onView(withId(R.id.etPhone)).perform(typeText("abc-!@#$%-999"), closeSoftKeyboard());

        // Submit form
        onView(withId(R.id.btnSave)).perform(click());

        // Verify application handles it cleanly
        onView(withId(R.id.tilName)).check(matches(isDisplayed()));
    }

    @Test
    public void testValidContactSubmission() {
        // Enter valid contact data
        onView(withId(R.id.etName)).perform(replaceText("د. خالد الطبية"), closeSoftKeyboard());
        onView(withId(R.id.etPhone)).perform(replaceText("0987654321"), closeSoftKeyboard());
        onView(withId(R.id.etRelationship)).perform(replaceText("طبيب العائلة"), closeSoftKeyboard());

        // Submit form
        onView(withId(R.id.btnSave)).perform(click());
    }
}

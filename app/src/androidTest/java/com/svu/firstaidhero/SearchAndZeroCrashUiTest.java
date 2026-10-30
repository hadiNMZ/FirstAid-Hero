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

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Automated Espresso UI Test verifying special character search input, Arabic diacritics,
 * rapid filter chip toggling, and zero-crash robustness in MainActivity / GuidesFragment.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SearchAndZeroCrashUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testSpecialCharacterAndDiacriticSearchDoesNotCrash() {
        // Type special characters in search box
        onView(withId(R.id.etSearch)).perform(
                replaceText("!@#$%^&*()_+=-[]{}|;:'\",.<>/?`~"),
                closeSoftKeyboard());
        onView(withId(R.id.etSearch)).check(matches(isDisplayed()));

        // Type Arabic with full diacritics (Tashkeel) and English digits
        onView(withId(R.id.etSearch)).perform(
                replaceText("سَكْتَةٌ قَلْبِيَّةٌ 123"),
                closeSoftKeyboard());
        onView(withId(R.id.etSearch)).check(matches(isDisplayed()));

        // Clear search box
        onView(withId(R.id.etSearch)).perform(
                replaceText(""),
                closeSoftKeyboard());
        onView(withId(R.id.etSearch)).check(matches(isDisplayed()));
    }

    @Test
    public void testRapidChipAndSortToggleZeroCrash() {
        // Toggle filter chips rapidly
        onView(withId(R.id.chipHigh)).perform(click());
        onView(withId(R.id.chipCommon)).perform(click());
        onView(withId(R.id.chipAll)).perform(click());

        // Toggle sort buttons
        onView(withId(R.id.btnSortAlpha)).perform(click());
        onView(withId(R.id.btnSortSevere)).perform(click());

        // Verify UI remains intact with zero crash
        onView(withId(R.id.rvEmergencies)).check(matches(isDisplayed()));
    }
}

package com.svu.firstaidhero;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.svu.firstaidhero.utils.Dialer;
import com.svu.firstaidhero.views.fragments.CasesFragment;
import com.svu.firstaidhero.views.fragments.ContactsFragment;
import com.svu.firstaidhero.views.fragments.GuidesFragment;
import com.svu.firstaidhero.views.fragments.KitFragment;
import com.svu.firstaidhero.views.fragments.SettingsFragment;

/** Hosts the five main tabs under a shared header. */
public class MainActivity extends AppCompatActivity {

    private static final String TAG_GUIDES = "guides";
    private static final String TAG_KIT = "kit";
    private static final String TAG_CASES = "cases";
    private static final String TAG_CONTACTS = "contacts";
    private static final String TAG_SETTINGS = "settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        findViewById(R.id.btnHeaderCall).setOnClickListener(
                v -> Dialer.dial(this, Dialer.EMERGENCY_NUMBER));

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_kit) {
                showTab(TAG_KIT);
            } else if (id == R.id.nav_cases) {
                showTab(TAG_CASES);
            } else if (id == R.id.nav_contacts) {
                showTab(TAG_CONTACTS);
            } else if (id == R.id.nav_settings) {
                showTab(TAG_SETTINGS);
            } else {
                showTab(TAG_GUIDES);
            }
            return true;
        });

        if (savedInstanceState == null) {
            showTab(TAG_GUIDES);
        }
    }

    /** Shows the tab with the given tag, keeping the others alive so their state survives. */
    private void showTab(String tag) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tx = fm.beginTransaction();

        for (Fragment f : fm.getFragments()) {
            if (!tag.equals(f.getTag())) tx.hide(f);
        }

        Fragment target = fm.findFragmentByTag(tag);
        if (target == null) {
            tx.add(R.id.fragmentContainer, createTab(tag), tag);
        } else {
            tx.show(target);
        }
        tx.commit();
    }

    private Fragment createTab(String tag) {
        switch (tag) {
            case TAG_KIT:
                return new KitFragment();
            case TAG_CASES:
                return new CasesFragment();
            case TAG_CONTACTS:
                return new ContactsFragment();
            case TAG_SETTINGS:
                return new SettingsFragment();
            default:
                return new GuidesFragment();
        }
    }
}

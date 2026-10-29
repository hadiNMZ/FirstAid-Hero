package com.svu.firstaidhero;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.svu.firstaidhero.utils.Dialer;
import com.svu.firstaidhero.views.fragments.CasesFragment;
import com.svu.firstaidhero.views.fragments.ContactsFragment;
import com.svu.firstaidhero.views.fragments.GuidesFragment;
import com.svu.firstaidhero.views.fragments.KitFragment;
import com.svu.firstaidhero.views.fragments.SettingsFragment;
import com.svu.firstaidhero.views.fragments.SosFragment;

/** Hosts the main app fragments with custom top header and bottom navigation bar. */
public class MainActivity extends AppCompatActivity {

    public static final String TAG_GUIDES = "guides";
    public static final String TAG_KIT = "kit";
    public static final String TAG_CASES = "cases";
    public static final String TAG_CONTACTS = "contacts";
    public static final String TAG_SETTINGS = "settings";
    public static final String TAG_SOS = "sos";

    private View navItemGuides;
    private ImageView ivNavCases;
    private TextView tvNavCases;

    private int primaryColor;
    private int outlineColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        primaryColor = ContextCompat.getColor(this, R.color.nabd_primary);
        outlineColor = ContextCompat.getColor(this, R.color.nabd_outline);

        // Header actions
        findViewById(R.id.btnHeaderContacts).setOnClickListener(v -> showTab(TAG_CONTACTS));
        findViewById(R.id.btnHeaderSettings).setOnClickListener(v -> showTab(TAG_SETTINGS));

        // Bottom Nav views
        navItemGuides = findViewById(R.id.navItemGuides);
        ivNavCases = findViewById(R.id.ivNavCases);
        tvNavCases = findViewById(R.id.tvNavCases);

        findViewById(R.id.btnNavCall110).setOnClickListener(
                v -> Dialer.dial(this, Dialer.EMERGENCY_NUMBER));

        findViewById(R.id.navItemSos).setOnClickListener(
                v -> showTab(TAG_SOS));

        if (navItemGuides != null) {
            navItemGuides.setOnClickListener(v -> showTab(TAG_GUIDES));
        }
        findViewById(R.id.navItemCases).setOnClickListener(v -> showTab(TAG_CASES));

        if (savedInstanceState == null) {
            showTab(TAG_GUIDES);
        }
    }

    /** Shows the tab with the given tag, keeping the others alive so their state survives. */
    public void showTab(String tag) {
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

        updateNavUI(tag);
    }

    private void updateNavUI(String tag) {
        boolean isGuides = TAG_GUIDES.equals(tag);
        boolean isCases = TAG_CASES.equals(tag);

        if (navItemGuides != null) {
            navItemGuides.setBackgroundColor(isGuides ? Color.parseColor("#1565C0") : Color.parseColor("#0D47A1"));
        }

        if (ivNavCases != null && tvNavCases != null) {
            int color = isCases ? primaryColor : outlineColor;
            ivNavCases.setImageTintList(ColorStateList.valueOf(color));
            tvNavCases.setTextColor(color);
        }
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
            case TAG_SOS:
                return new SosFragment();
            default:
                return new GuidesFragment();
        }
    }
}

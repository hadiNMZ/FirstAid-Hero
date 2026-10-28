package com.svu.firstaidhero.views.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.svu.firstaidhero.R;
import com.svu.firstaidhero.utils.AppPrefs;
import com.svu.firstaidhero.utils.Dialer;
import com.svu.firstaidhero.utils.EmergencyCategory;
import com.svu.firstaidhero.utils.ImageResources;
import com.svu.firstaidhero.utils.TextSanitizer;
import com.svu.firstaidhero.viewmodels.EmergencyViewModel;
import com.svu.firstaidhero.views.adapters.StepAdapter;

import java.util.Locale;

public class EmergencyDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CASE_ID = "EXTRA_CASE_ID";
    private static final int INVALID_ID = -1;
    private static final String STATE_TIMER_BASE = "timer_base";
    private static final String STATE_TIMER_ELAPSED = "timer_elapsed";

    // Stopwatch state: while running, elapsed = now - base; while paused, it's frozen
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long timerBase;
    private long pausedElapsed;
    private boolean timerRunning;
    private TextView tvTimer;
    private MaterialButton btnTimerToggle;

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            showElapsed(SystemClock.elapsedRealtime() - timerBase);
            handler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_detail);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        int caseId = getIntent().getIntExtra(EXTRA_CASE_ID, INVALID_ID);
        if (caseId == INVALID_ID) {
            finish(); // nothing to show without an id
            return;
        }

        if (AppPrefs.keepScreenOn(this)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        View.OnClickListener callEmergency = v -> Dialer.dial(this, Dialer.EMERGENCY_NUMBER);
        findViewById(R.id.btnTopCall).setOnClickListener(callEmergency);
        findViewById(R.id.btnCallInline).setOnClickListener(callEmergency);
        findViewById(R.id.btnCallEmergency).setOnClickListener(callEmergency);

        setupTimer(savedInstanceState);

        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvBadge = findViewById(R.id.tvDetailBadge);
        TextView tvCategory = findViewById(R.id.tvDetailCategory);
        TextView tvSessionCategory = findViewById(R.id.tvSessionCategory);
        TextView tvSymptoms = findViewById(R.id.tvDetailSymptoms);
        TextView tvStepCount = findViewById(R.id.tvStepCount);
        ImageView ivHero = findViewById(R.id.ivHero);
        RecyclerView rvSteps = findViewById(R.id.rvSteps);

        StepAdapter stepAdapter = new StepAdapter();
        stepAdapter.setLargeText(AppPrefs.largeStepText(this));
        rvSteps.setLayoutManager(new LinearLayoutManager(this));
        rvSteps.setAdapter(stepAdapter);

        EmergencyViewModel viewModel =
                new ViewModelProvider(this).get(EmergencyViewModel.class);

        viewModel.getEmergencyById(caseId).observe(this, emergency -> {
            if (emergency == null) return;
            boolean high = "High".equalsIgnoreCase(emergency.getDangerLevel());
            int accent = ContextCompat.getColor(this,
                    high ? R.color.nabd_emergency : R.color.nabd_info_blue);
            EmergencyCategory category = EmergencyCategory.of(emergency);

            tvTitle.setText(TextSanitizer.cleanTitle(emergency.getTitle()));
            tvSymptoms.setText(TextSanitizer.cleanSymptoms(emergency.getSymptoms()));

            tvBadge.setText(high ? R.string.critical_alert : R.string.minor_alert);
            tvBadge.setBackgroundResource(high ? R.drawable.bg_badge_high : R.drawable.bg_badge_minor);
            tvBadge.setTextColor(accent);

            tvCategory.setText(category.label);
            tvCategory.setCompoundDrawablesRelativeWithIntrinsicBounds(category.icon, 0, 0, 0);
            TextViewCompat.setCompoundDrawableTintList(tvCategory,
                    ContextCompat.getColorStateList(this, high ? R.color.nabd_emergency : R.color.nabd_info_blue));
            tvSessionCategory.setText(category.label);

            int hero = ImageResources.find(this, category.heroImageName());
            ivHero.setVisibility(hero != 0 ? View.VISIBLE : View.GONE);
            if (hero != 0) ivHero.setImageResource(hero);

            stepAdapter.setCritical(high);
        });

        viewModel.getStepsForEmergency(caseId).observe(this, steps -> {
            stepAdapter.submitList(steps);
            int count = steps == null ? 0 : steps.size();
            tvStepCount.setText(getResources().getQuantityString(R.plurals.steps_count, count, count));
        });
    }

    private void setupTimer(Bundle savedInstanceState) {
        tvTimer = findViewById(R.id.tvTimer);
        btnTimerToggle = findViewById(R.id.btnTimerToggle);

        if (savedInstanceState != null) {
            pausedElapsed = savedInstanceState.getLong(STATE_TIMER_ELAPSED);
            long base = savedInstanceState.getLong(STATE_TIMER_BASE);
            if (base > 0) {
                timerBase = base;
                timerRunning = true;
            }
        }
        showElapsed(timerRunning ? SystemClock.elapsedRealtime() - timerBase : pausedElapsed);
        updateTimerButton();

        btnTimerToggle.setOnClickListener(v -> {
            if (timerRunning) {
                pausedElapsed = SystemClock.elapsedRealtime() - timerBase;
                timerRunning = false;
                handler.removeCallbacks(tick);
            } else {
                timerBase = SystemClock.elapsedRealtime() - pausedElapsed;
                timerRunning = true;
                handler.post(tick);
            }
            updateTimerButton();
        });

        findViewById(R.id.btnTimerReset).setOnClickListener(v -> {
            timerRunning = false;
            pausedElapsed = 0;
            handler.removeCallbacks(tick);
            showElapsed(0);
            updateTimerButton();
        });
    }

    private void updateTimerButton() {
        btnTimerToggle.setText(timerRunning ? R.string.timer_pause : R.string.timer_start);
        btnTimerToggle.setIconResource(timerRunning ? R.drawable.ic_pause : R.drawable.ic_play);
    }

    private void showElapsed(long millis) {
        long seconds = millis / 1000;
        tvTimer.setText(String.format(Locale.US, "%02d:%02d", seconds / 60, seconds % 60));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (timerRunning) handler.post(tick);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(tick);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_TIMER_BASE, timerRunning ? timerBase : 0);
        outState.putLong(STATE_TIMER_ELAPSED, pausedElapsed);
    }
}

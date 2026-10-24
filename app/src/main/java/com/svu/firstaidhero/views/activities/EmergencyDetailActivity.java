package com.svu.firstaidhero.views.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.svu.firstaidhero.R;
import com.svu.firstaidhero.utils.TextSanitizer;
import com.svu.firstaidhero.viewmodels.EmergencyViewModel;
import com.svu.firstaidhero.views.adapters.StepAdapter;

public class EmergencyDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CASE_ID = "EXTRA_CASE_ID";
    private static final int INVALID_ID = -1;
    private static final String EMERGENCY_NUMBER = "110";

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

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvBadge = findViewById(R.id.tvDetailBadge);
        TextView tvSymptoms = findViewById(R.id.tvDetailSymptoms);
        RecyclerView rvSteps = findViewById(R.id.rvSteps);
        MaterialButton btnCall = findViewById(R.id.btnCallEmergency);

        StepAdapter stepAdapter = new StepAdapter();
        rvSteps.setLayoutManager(new LinearLayoutManager(this));
        rvSteps.setAdapter(stepAdapter);

        EmergencyViewModel viewModel =
                new ViewModelProvider(this).get(EmergencyViewModel.class);

        viewModel.getEmergencyById(caseId).observe(this, emergency -> {
            if (emergency == null) return;
            tvTitle.setText(TextSanitizer.cleanTitle(emergency.getTitle()));
            tvSymptoms.setText(TextSanitizer.cleanSymptoms(emergency.getSymptoms()));
            tvBadge.setText(TextSanitizer.getArabicDangerLevel(emergency.getDangerLevel()));
            tvBadge.setBackgroundResource(
                    "High".equalsIgnoreCase(emergency.getDangerLevel())
                            ? R.drawable.bg_badge_high
                            : R.drawable.bg_badge_minor);
        });

        viewModel.getStepsForEmergency(caseId).observe(this, stepAdapter::submitList);

        btnCall.setOnClickListener(v -> dialEmergency());
    }

    /** Opens the phone dialer with the number filled in; the user presses call. */
    private void dialEmergency() {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + EMERGENCY_NUMBER));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.dialer_not_found, Toast.LENGTH_SHORT).show();
        }
    }
}
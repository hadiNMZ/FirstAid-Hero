package com.svu.firstaidhero.views.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.svu.firstaidhero.R;
import com.svu.firstaidhero.database.entities.TreatedCase;
import com.svu.firstaidhero.utils.CaseShare;
import com.svu.firstaidhero.utils.Dialer;
import com.svu.firstaidhero.utils.ImageLoader;
import com.svu.firstaidhero.utils.Triage;
import com.svu.firstaidhero.viewmodels.ContactAndLogViewModel;

/** Full report for one treated case, with share and edit. */
public class CaseDetailActivity extends AppCompatActivity {

    public static final String EXTRA_LOG_ID = "EXTRA_LOG_ID";

    private TreatedCase current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_case_detail);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        int logId = getIntent().getIntExtra(EXTRA_LOG_ID, -1);
        if (logId == -1) {
            finish();
            return;
        }

        ((TextView) findViewById(R.id.tvTopBarTitle)).setText(R.string.case_report_title);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnTopCall).setOnClickListener(
                v -> Dialer.dial(this, Dialer.EMERGENCY_NUMBER));

        TextView tvTriage = findViewById(R.id.tvReportTriage);
        TextView tvNumber = findViewById(R.id.tvReportNumber);
        TextView tvTitle = findViewById(R.id.tvReportTitle);
        TextView tvDate = findViewById(R.id.tvReportDate);
        TextView tvNotes = findViewById(R.id.tvReportNotes);
        ImageView ivPhoto = findViewById(R.id.ivReportPhoto);

        ContactAndLogViewModel viewModel =
                new ViewModelProvider(this).get(ContactAndLogViewModel.class);
        viewModel.getTreatedCaseById(logId).observe(this, c -> {
            if (c == null) {
                // Deleted from the edit screen
                finish();
                return;
            }
            current = c;
            String level = c.getTriageLevel();
            tvTriage.setText(Triage.label(level));
            tvTriage.setTextColor(ContextCompat.getColor(this, Triage.color(level)));
            tvTriage.setBackgroundResource(Triage.pill(level));
            tvNumber.setText(getString(R.string.case_number, c.getLogId()));
            tvTitle.setText(c.getDescription());
            tvDate.setText(c.getDateTreated());
            tvNotes.setText(c.getProceduralNotes());

            String photo = c.getPhotoUri();
            Bitmap bitmap = (photo == null || photo.isEmpty())
                    ? null : ImageLoader.load(this, Uri.parse(photo), 1080);
            ivPhoto.setVisibility(bitmap == null ? View.GONE : View.VISIBLE);
            ivPhoto.setImageBitmap(bitmap);
        });

        findViewById(R.id.btnReportShare).setOnClickListener(v -> {
            if (current != null) CaseShare.share(this, current);
        });
        findViewById(R.id.btnReportEdit).setOnClickListener(v -> {
            Intent intent = new Intent(this, LogIncidentActivity.class);
            intent.putExtra(LogIncidentActivity.EXTRA_LOG_ID, logId);
            startActivity(intent);
        });
    }
}

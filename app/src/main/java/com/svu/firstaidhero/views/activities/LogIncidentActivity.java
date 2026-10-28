package com.svu.firstaidhero.views.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.svu.firstaidhero.R;
import com.svu.firstaidhero.database.entities.TreatedCase;
import com.svu.firstaidhero.utils.CaseDates;
import com.svu.firstaidhero.utils.Dialer;
import com.svu.firstaidhero.utils.ImageLoader;
import com.svu.firstaidhero.utils.Triage;
import com.svu.firstaidhero.viewmodels.ContactAndLogViewModel;

/** Logs a new treated case, or edits/deletes an existing one when EXTRA_LOG_ID is passed. */
public class LogIncidentActivity extends AppCompatActivity {

    public static final String EXTRA_LOG_ID = "EXTRA_LOG_ID";
    private static final String STATE_PHOTO = "photo_uri";

    private Uri photoUri;
    private ImageView ivPhotoPreview;
    private View photoPlaceholder;
    private TextInputEditText etDescription;
    private TextInputEditText etNotes;
    private ChipGroup chipGroupTriage;
    private TextView tvDateTime;
    private ContactAndLogViewModel viewModel;

    // The case being edited; null while logging a new one
    private TreatedCase editing;

    // Opens the system picker; the permission is persisted so the log can show the photo later
    private final ActivityResultLauncher<String[]> pickPhoto =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;
                try {
                    getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException ignored) {
                    // Some providers don't offer persistable permissions; the URI still works now
                }
                showPhoto(uri);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_incident);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        int logId = getIntent().getIntExtra(EXTRA_LOG_ID, -1);
        boolean isEdit = logId != -1;

        ((TextView) findViewById(R.id.tvTopBarTitle))
                .setText(isEdit ? R.string.edit_case_title : R.string.log_incident);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnTopCall).setOnClickListener(
                v -> Dialer.dial(this, Dialer.EMERGENCY_NUMBER));

        tvDateTime = findViewById(R.id.tvDateTime);
        tvDateTime.setText(CaseDates.now());

        TextInputLayout tilDescription = findViewById(R.id.tilDescription);
        TextInputLayout tilNotes = findViewById(R.id.tilNotes);
        etDescription = findViewById(R.id.etDescription);
        etNotes = findViewById(R.id.etNotes);
        ChipGroup chipGroupQuick = findViewById(R.id.chipGroupQuick);
        chipGroupTriage = findViewById(R.id.chipGroupTriage);
        ivPhotoPreview = findViewById(R.id.ivPhotoPreview);
        photoPlaceholder = findViewById(R.id.photoPlaceholder);
        MaterialButton btnDelete = findViewById(R.id.btnDelete);

        // Quick-description chips fill the description field
        for (int i = 0; i < chipGroupQuick.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupQuick.getChildAt(i);
            chip.setOnClickListener(v -> etDescription.setText(chip.getText()));
        }

        findViewById(R.id.boxPhoto).setOnClickListener(
                v -> pickPhoto.launch(new String[]{"image/*"}));

        if (savedInstanceState != null) {
            String saved = savedInstanceState.getString(STATE_PHOTO);
            if (saved != null) showPhoto(Uri.parse(saved));
        }

        viewModel = new ViewModelProvider(this).get(ContactAndLogViewModel.class);

        if (isEdit) {
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v -> confirmDelete());
            viewModel.getTreatedCaseById(logId).observe(this, c -> {
                if (c == null) {
                    finish();
                    return;
                }
                boolean firstLoad = editing == null;
                editing = c;
                // Fill the form once only, so later updates don't overwrite what the user typed
                if (firstLoad && savedInstanceState == null) fillForm(c);
            });
        }

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            String description = textOf(etDescription.getText());
            String notes = textOf(etNotes.getText());
            tilDescription.setError(
                    description.isEmpty() ? getString(R.string.error_required) : null);
            tilNotes.setError(notes.isEmpty() ? getString(R.string.error_required) : null);
            if (description.isEmpty() || notes.isEmpty()) return;

            String photo = photoUri != null ? photoUri.toString() : null;
            if (isEdit) {
                if (editing == null) return; // still loading
                editing.setDescription(description);
                editing.setProceduralNotes(notes);
                editing.setPhotoUri(photo);
                editing.setTriageLevel(selectedTriage());
                viewModel.updateTreatedCase(editing);
            } else {
                TreatedCase c = new TreatedCase(description, tvDateTime.getText().toString(), notes, photo);
                c.setTriageLevel(selectedTriage());
                viewModel.insertTreatedCase(c);
            }
            Toast.makeText(this, R.string.case_saved, Toast.LENGTH_SHORT).show();
            finish();
        });
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
    }

    private void fillForm(TreatedCase c) {
        tvDateTime.setText(c.getDateTreated());
        etDescription.setText(c.getDescription());
        etNotes.setText(c.getProceduralNotes());
        if (c.getPhotoUri() != null && !c.getPhotoUri().isEmpty()) {
            showPhoto(Uri.parse(c.getPhotoUri()));
        }
        int chipId = Triage.CRITICAL.equals(c.getTriageLevel()) ? R.id.chipCritical
                : Triage.URGENT.equals(c.getTriageLevel()) ? R.id.chipUrgent
                : R.id.chipStable;
        chipGroupTriage.check(chipId);
    }

    private String selectedTriage() {
        int checked = chipGroupTriage.getCheckedChipId();
        if (checked == R.id.chipCritical) return Triage.CRITICAL;
        if (checked == R.id.chipUrgent) return Triage.URGENT;
        return Triage.STABLE;
    }

    private void showPhoto(Uri uri) {
        Bitmap bitmap = ImageLoader.load(this, uri, 1080);
        if (bitmap == null) return;
        photoUri = uri;
        ivPhotoPreview.setImageBitmap(bitmap);
        ivPhotoPreview.setVisibility(View.VISIBLE);
        photoPlaceholder.setVisibility(View.GONE);
    }

    private void confirmDelete() {
        if (editing == null) return;
        new MaterialAlertDialogBuilder(this)
                .setMessage(R.string.delete_case_confirm)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.deleteTreatedCase(editing);
                    Toast.makeText(this, R.string.case_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (photoUri != null) outState.putString(STATE_PHOTO, photoUri.toString());
    }

    private static String textOf(Editable text) {
        return text == null ? "" : text.toString().trim();
    }
}

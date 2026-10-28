package com.svu.firstaidhero.views.activities;

import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.svu.firstaidhero.R;
import com.svu.firstaidhero.database.entities.CustomContact;
import com.svu.firstaidhero.utils.Dialer;
import com.svu.firstaidhero.viewmodels.ContactAndLogViewModel;

/** Adds a new contact, or edits/deletes an existing one when EXTRA_CONTACT_ID is passed. */
public class AddContactActivity extends AppCompatActivity {

    public static final String EXTRA_CONTACT_ID = "EXTRA_CONTACT_ID";
    private static final int NEW_CONTACT = -1;

    private ContactAndLogViewModel viewModel;
    private TextInputEditText etName;
    private TextInputEditText etPhone;
    private TextInputEditText etRelationship;
    private ChipGroup chipGroupRelation;

    // The contact being edited; null while adding a new one
    private CustomContact editing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        int contactId = getIntent().getIntExtra(EXTRA_CONTACT_ID, NEW_CONTACT);
        boolean isEdit = contactId != NEW_CONTACT;

        TextView tvTitle = findViewById(R.id.tvTopBarTitle);
        tvTitle.setText(isEdit ? R.string.edit_contact_title : R.string.add_contact_title);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnTopCall).setOnClickListener(
                v -> Dialer.dial(this, Dialer.EMERGENCY_NUMBER));

        TextInputLayout tilName = findViewById(R.id.tilName);
        TextInputLayout tilPhone = findViewById(R.id.tilPhone);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etRelationship = findViewById(R.id.etRelationship);
        chipGroupRelation = findViewById(R.id.chipGroupRelation);
        MaterialButton btnSave = findViewById(R.id.btnSave);
        MaterialButton btnDelete = findViewById(R.id.btnDelete);

        // Picking a relationship chip fills the free-text field, which stays editable
        chipGroupRelation.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            Chip chip = group.findViewById(checkedIds.get(0));
            etRelationship.setText(chip.getText());
        });

        viewModel = new ViewModelProvider(this).get(ContactAndLogViewModel.class);

        if (isEdit) {
            btnSave.setText(R.string.save_changes);
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v -> confirmDelete());
            viewModel.getContactById(contactId).observe(this, contact -> {
                if (contact == null) {
                    // Deleted elsewhere (or just now); nothing left to edit
                    finish();
                    return;
                }
                boolean firstLoad = editing == null;
                editing = contact;
                // Fill the form once only, so later updates don't overwrite what the user typed
                if (firstLoad && savedInstanceState == null) fillForm(contact);
            });
        }

        btnSave.setOnClickListener(v -> {
            String name = textOf(etName.getText());
            String phone = textOf(etPhone.getText());
            tilName.setError(name.isEmpty() ? getString(R.string.error_required) : null);
            tilPhone.setError(phone.isEmpty() ? getString(R.string.error_required) : null);
            if (name.isEmpty() || phone.isEmpty()) return;

            String relationship = textOf(etRelationship.getText());
            if (isEdit) {
                if (editing == null) return; // still loading
                editing.setName(name);
                editing.setPhoneNumber(phone);
                editing.setRelationship(relationship);
                viewModel.updateContact(editing);
            } else {
                viewModel.insertContact(new CustomContact(name, phone, relationship));
            }
            Toast.makeText(this, R.string.contact_saved, Toast.LENGTH_SHORT).show();
            finish();
        });
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
    }

    private void fillForm(CustomContact contact) {
        etName.setText(contact.getName());
        etPhone.setText(contact.getPhoneNumber());
        etRelationship.setText(contact.getRelationship());
        for (int i = 0; i < chipGroupRelation.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupRelation.getChildAt(i);
            if (chip.getText().toString().equals(contact.getRelationship())) {
                chip.setChecked(true);
                break;
            }
        }
    }

    private void confirmDelete() {
        if (editing == null) return;
        new MaterialAlertDialogBuilder(this)
                .setMessage(getString(R.string.delete_contact_confirm, editing.getName()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.deleteContact(editing);
                    Toast.makeText(this, R.string.contact_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .show();
    }

    private static String textOf(Editable text) {
        return text == null ? "" : text.toString().trim();
    }
}

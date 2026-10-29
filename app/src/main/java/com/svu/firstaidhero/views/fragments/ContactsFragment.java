package com.svu.firstaidhero.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.svu.firstaidhero.R;
import com.svu.firstaidhero.database.entities.CustomContact;
import com.svu.firstaidhero.utils.Dialer;
import com.svu.firstaidhero.viewmodels.ContactAndLogViewModel;
import com.svu.firstaidhero.views.activities.AddContactActivity;
import com.svu.firstaidhero.views.adapters.ContactAdapter;

/** National hotlines plus the user's own saved contacts. */
public class ContactsFragment extends Fragment {

    // Syrian national emergency lines
    private static final String NUMBER_POLICE = "112";
    private static final String NUMBER_FIRE = "113";
    private static final String NUMBER_TRAFFIC = "115";
    private static final String NUMBER_RED_CRESCENT = "133";

    private ContactAndLogViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        // 1. Central Ambulance (110)
        view.findViewById(R.id.tileAmbulance).setOnClickListener(
                v -> Dialer.dial(requireContext(), Dialer.EMERGENCY_NUMBER));

        // 2. شرطة النجدة (112)
        setupTile(view.findViewById(R.id.tilePolice), NUMBER_POLICE,
                R.string.line_police, R.drawable.ic_police, R.color.nabd_info_blue);

        // 3. الإطفاء / الدفاع المدني (113)
        setupTile(view.findViewById(R.id.tileFire), NUMBER_FIRE,
                R.string.line_fire, R.drawable.ic_fire, R.color.nabd_emergency);

        // 4. شرطة المرور (115)
        setupTile(view.findViewById(R.id.tileTraffic), NUMBER_TRAFFIC,
                R.string.line_traffic, R.drawable.ic_police, R.color.nabd_info_blue);

        // 5. الهلال الأحمر السوري (133)
        setupTile(view.findViewById(R.id.tileRedCrescent), NUMBER_RED_CRESCENT,
                R.string.line_red_crescent, R.drawable.ic_health, R.color.nabd_emergency);

        RecyclerView rvContacts = view.findViewById(R.id.rvContacts);
        View emptyState = view.findViewById(R.id.emptyContacts);
        TextView tvCount = view.findViewById(R.id.tvContactsCount);

        ContactAdapter adapter = new ContactAdapter();
        adapter.setListener(new ContactAdapter.Listener() {
            @Override
            public void onEdit(CustomContact contact) {
                Intent intent = new Intent(requireContext(), AddContactActivity.class);
                intent.putExtra(AddContactActivity.EXTRA_CONTACT_ID, contact.getContactId());
                startActivity(intent);
            }

            @Override
            public void onDelete(CustomContact contact) {
                confirmDelete(contact);
            }
        });
        rvContacts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvContacts.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ContactAndLogViewModel.class);
        viewModel.getAllContacts().observe(getViewLifecycleOwner(), contacts -> {
            adapter.submitList(contacts);
            int count = contacts == null ? 0 : contacts.size();
            tvCount.setText(getString(R.string.registered_count, count));
            emptyState.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
        });

        view.findViewById(R.id.btnAddContact).setOnClickListener(
                v -> startActivity(new Intent(requireContext(), AddContactActivity.class)));
    }

    private void setupTile(View tile, String number, @StringRes int label,
                           @DrawableRes int icon, @ColorRes int color) {
        ImageView ivIcon = tile.findViewById(R.id.ivHotlineIcon);
        ivIcon.setImageResource(icon);
        ivIcon.setColorFilter(ContextCompat.getColor(requireContext(), color));
        ((TextView) tile.findViewById(R.id.tvHotlineNumber)).setText(number);
        ((TextView) tile.findViewById(R.id.tvHotlineLabel)).setText(label);
        tile.setOnClickListener(v -> Dialer.dial(requireContext(), number));
    }

    private void confirmDelete(CustomContact contact) {
        new MaterialAlertDialogBuilder(requireContext())
                .setMessage(getString(R.string.delete_contact_confirm, contact.getName()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.deleteContact(contact);
                    Toast.makeText(requireContext(), R.string.contact_deleted, Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}

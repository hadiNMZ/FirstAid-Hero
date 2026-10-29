package com.svu.firstaidhero.views.fragments;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.svu.firstaidhero.MainActivity;
import com.svu.firstaidhero.R;
import com.svu.firstaidhero.database.entities.Emergency;
import com.svu.firstaidhero.database.entities.TreatedCase;
import com.svu.firstaidhero.utils.AppPrefs;
import com.svu.firstaidhero.utils.CaseShare;
import com.svu.firstaidhero.viewmodels.ContactAndLogViewModel;
import com.svu.firstaidhero.viewmodels.EmergencyViewModel;

import java.util.ArrayList;
import java.util.List;

/** Preferences, database status, medical kit access, and case-log export/clear. */
public class SettingsFragment extends Fragment {

    private List<TreatedCase> cases = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView tvProtocolCount = view.findViewById(R.id.tvProtocolCount);
        TextView tvCriticalCount = view.findViewById(R.id.tvCriticalCount);

        // Click listener to navigate to the Medical Kit Fragment tab
        view.findViewById(R.id.cardMedicalKit).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showTab(MainActivity.TAG_KIT);
            }
        });

        EmergencyViewModel emergencies = new ViewModelProvider(this).get(EmergencyViewModel.class);
        emergencies.getAllEmergencies().observe(getViewLifecycleOwner(), list -> {
            int total = list == null ? 0 : list.size();
            int critical = 0;
            if (list != null) {
                for (Emergency e : list) {
                    if ("High".equalsIgnoreCase(e.getDangerLevel())) critical++;
                }
            }
            tvProtocolCount.setText(String.valueOf(total));
            tvCriticalCount.setText(String.valueOf(critical));
        });

        MaterialSwitch switchKeepOn = view.findViewById(R.id.switchKeepScreenOn);
        switchKeepOn.setChecked(AppPrefs.keepScreenOn(requireContext()));
        switchKeepOn.setOnCheckedChangeListener(
                (b, checked) -> AppPrefs.setKeepScreenOn(requireContext(), checked));

        MaterialSwitch switchLarge = view.findViewById(R.id.switchLargeText);
        switchLarge.setChecked(AppPrefs.largeStepText(requireContext()));
        switchLarge.setOnCheckedChangeListener(
                (b, checked) -> AppPrefs.setLargeStepText(requireContext(), checked));

        ContactAndLogViewModel log = new ViewModelProvider(this).get(ContactAndLogViewModel.class);
        log.getAllTreatedCases().observe(getViewLifecycleOwner(),
                list -> cases = list != null ? list : new ArrayList<>());

        view.findViewById(R.id.btnExportCases).setOnClickListener(v -> {
            if (cases.isEmpty()) {
                Toast.makeText(requireContext(), R.string.pref_nothing_to_export, Toast.LENGTH_SHORT).show();
            } else {
                CaseShare.shareAll(requireContext(), cases);
            }
        });
        view.findViewById(R.id.btnClearCases).setOnClickListener(v -> {
            if (cases.isEmpty()) {
                Toast.makeText(requireContext(), R.string.pref_nothing_to_export, Toast.LENGTH_SHORT).show();
                return;
            }
            new MaterialAlertDialogBuilder(requireContext())
                    .setMessage(R.string.pref_clear_confirm)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.delete, (dialog, which) -> {
                        for (TreatedCase c : new ArrayList<>(cases)) log.deleteTreatedCase(c);
                        Toast.makeText(requireContext(), R.string.pref_cleared, Toast.LENGTH_SHORT).show();
                    })
                    .show();
        });

        TextView tvVersion = view.findViewById(R.id.tvVersion);
        tvVersion.setText(getString(R.string.version_label, versionName()));
    }

    private String versionName() {
        try {
            PackageInfo info = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }
}

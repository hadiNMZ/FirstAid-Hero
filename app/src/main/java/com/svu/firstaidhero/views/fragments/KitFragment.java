package com.svu.firstaidhero.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.svu.firstaidhero.R;
import com.svu.firstaidhero.database.entities.KitItem;
import com.svu.firstaidhero.utils.CaseShare;
import com.svu.firstaidhero.utils.KitStatus;
import com.svu.firstaidhero.viewmodels.KitViewModel;
import com.svu.firstaidhero.views.adapters.KitAdapter;

import java.util.ArrayList;
import java.util.List;

/** First-aid kit inventory: readiness, shortages and expiry, with quantity steppers. */
public class KitFragment extends Fragment {

    private KitViewModel viewModel;
    private KitAdapter adapter;
    private ChipGroup chipGroup;
    private List<KitItem> allItems = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_kit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        CircularProgressIndicator progress = view.findViewById(R.id.progressReadiness);
        TextView tvReadiness = view.findViewById(R.id.tvReadiness);
        TextView tvSummary = view.findViewById(R.id.tvKitSummary);
        View btnShare = view.findViewById(R.id.btnShareShortages);
        chipGroup = view.findViewById(R.id.chipGroupKit);
        RecyclerView rvKit = view.findViewById(R.id.rvKit);

        adapter = new KitAdapter();
        adapter.setOnQuantityChangeListener((item, newQuantity) -> {
            item.setQuantity(newQuantity);
            viewModel.updateItem(item);
        });
        rvKit.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvKit.setAdapter(adapter);
        // Quantity changes rebind in place; skip the change animation flicker
        rvKit.setItemAnimator(null);

        viewModel = new ViewModelProvider(this).get(KitViewModel.class);
        viewModel.getAllItems().observe(getViewLifecycleOwner(), items -> {
            allItems = items != null ? items : new ArrayList<>();

            int ok = 0;
            int shortages = 0;
            int expiring = 0;
            for (KitItem item : allItems) {
                KitStatus status = KitStatus.of(item);
                if (status.isProblem()) shortages++;
                else ok++;
                if (status == KitStatus.EXPIRING) expiring++;
            }
            int percent = allItems.isEmpty() ? 0 : Math.round(100f * ok / allItems.size());
            progress.setProgressCompat(percent, true);
            progress.setIndicatorColor(requireContext().getColor(
                    percent == 100 ? R.color.nabd_primary : R.color.nabd_emergency));
            tvReadiness.setText(getString(R.string.kit_ready_percent, percent));
            tvSummary.setText(getString(R.string.kit_summary, allItems.size(), shortages, expiring));
            btnShare.setVisibility(shortages > 0 ? View.VISIBLE : View.GONE);

            setChip(R.id.chipKitAll, getString(R.string.filter_all), allItems.size());
            setChip(R.id.chipKitBleeding, getString(R.string.kit_chip_bleeding), count(KitItem.CATEGORY_BLEEDING));
            setChip(R.id.chipKitBurns, getString(R.string.kit_chip_burns), count(KitItem.CATEGORY_BURNS));
            setChip(R.id.chipKitTools, getString(R.string.kit_chip_tools), count(KitItem.CATEGORY_TOOLS));
            applyFilter();
        });

        chipGroup.setOnCheckedStateChangeListener((group, ids) -> applyFilter());
        btnShare.setOnClickListener(v -> shareShortages());
    }

    private int count(String category) {
        int n = 0;
        for (KitItem item : allItems) {
            if (category.equals(item.getCategory())) n++;
        }
        return n;
    }

    private void setChip(int id, String label, int count) {
        Chip chip = chipGroup.findViewById(id);
        chip.setText(getString(R.string.filter_count, label, count));
    }

    private void applyFilter() {
        int checked = chipGroup.getCheckedChipId();
        String category = checked == R.id.chipKitBleeding ? KitItem.CATEGORY_BLEEDING
                : checked == R.id.chipKitBurns ? KitItem.CATEGORY_BURNS
                : checked == R.id.chipKitTools ? KitItem.CATEGORY_TOOLS
                : null;
        List<KitItem> result = new ArrayList<>();
        for (KitItem item : allItems) {
            if (category == null || category.equals(item.getCategory())) result.add(item);
        }
        adapter.submit(result);
    }

    /** Shares a shopping list of everything that is low or expired. */
    private void shareShortages() {
        StringBuilder sb = new StringBuilder(getString(R.string.kit_shortage_list_title));
        for (KitItem item : allItems) {
            if (!KitStatus.of(item).isProblem()) continue;
            int needed = Math.max(item.getRequiredQuantity() - item.getQuantity(), 0);
            if (needed == 0) needed = item.getRequiredQuantity(); // expired: replace all
            sb.append('\n').append(getString(R.string.kit_shortage_line, item.getName(), needed, item.getUnit()));
        }
        CaseShare.shareText(requireContext(), sb.toString());
    }
}

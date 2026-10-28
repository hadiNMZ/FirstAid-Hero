package com.svu.firstaidhero.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.android.material.textfield.TextInputEditText;
import com.svu.firstaidhero.R;
import com.svu.firstaidhero.database.entities.TreatedCase;
import com.svu.firstaidhero.utils.CaseShare;
import com.svu.firstaidhero.utils.Triage;
import com.svu.firstaidhero.viewmodels.ContactAndLogViewModel;
import com.svu.firstaidhero.views.activities.CaseDetailActivity;
import com.svu.firstaidhero.views.activities.LogIncidentActivity;
import com.svu.firstaidhero.views.adapters.TreatedCaseAdapter;

import java.util.ArrayList;
import java.util.List;

/** Log of first-aid cases the user has treated, with stats, search and triage filters. */
public class CasesFragment extends Fragment {

    private TreatedCaseAdapter adapter;
    private RecyclerView rvCases;
    private TextView tvEmpty;
    private ChipGroup chipGroupTriage;
    private List<TreatedCase> allCases = new ArrayList<>();
    private String query = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cases, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rvCases = view.findViewById(R.id.rvCases);
        tvEmpty = view.findViewById(R.id.tvCasesEmpty);
        chipGroupTriage = view.findViewById(R.id.chipGroupTriage);
        TextView tvStatTotal = view.findViewById(R.id.tvStatTotal);
        TextView tvStatCritical = view.findViewById(R.id.tvStatCritical);
        TextInputEditText etSearch = view.findViewById(R.id.etCasesSearch);

        adapter = new TreatedCaseAdapter();
        adapter.setListener(new TreatedCaseAdapter.Listener() {
            @Override
            public void onOpenReport(TreatedCase c) {
                Intent intent = new Intent(requireContext(), CaseDetailActivity.class);
                intent.putExtra(CaseDetailActivity.EXTRA_LOG_ID, c.getLogId());
                startActivity(intent);
            }

            @Override
            public void onShare(TreatedCase c) {
                CaseShare.share(requireContext(), c);
            }

            @Override
            public void onEdit(TreatedCase c) {
                Intent intent = new Intent(requireContext(), LogIncidentActivity.class);
                intent.putExtra(LogIncidentActivity.EXTRA_LOG_ID, c.getLogId());
                startActivity(intent);
            }
        });
        rvCases.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCases.setAdapter(adapter);

        ContactAndLogViewModel viewModel =
                new ViewModelProvider(this).get(ContactAndLogViewModel.class);
        viewModel.getAllTreatedCases().observe(getViewLifecycleOwner(), cases -> {
            allCases = cases != null ? cases : new ArrayList<>();
            int critical = count(Triage.CRITICAL);
            tvStatTotal.setText(String.valueOf(allCases.size()));
            tvStatCritical.setText(String.valueOf(critical));
            setChip(R.id.chipCasesAll, getString(R.string.filter_all), allCases.size());
            setChip(R.id.chipCasesCritical, getString(R.string.triage_short_critical), critical);
            setChip(R.id.chipCasesUrgent, getString(R.string.triage_short_urgent), count(Triage.URGENT));
            setChip(R.id.chipCasesStable, getString(R.string.triage_short_stable), count(Triage.STABLE));
            applyFilters();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                query = s.toString().trim();
                applyFilters();
            }
        });
        chipGroupTriage.setOnCheckedStateChangeListener((group, ids) -> applyFilters());

        view.findViewById(R.id.bannerNewCase).setOnClickListener(
                v -> startActivity(new Intent(requireContext(), LogIncidentActivity.class)));
    }

    private int count(String level) {
        int n = 0;
        for (TreatedCase c : allCases) {
            if (level.equals(c.getTriageLevel())) n++;
        }
        return n;
    }

    private void setChip(int id, String label, int count) {
        Chip chip = chipGroupTriage.findViewById(id);
        chip.setText(getString(R.string.filter_count, label, count));
    }

    private void applyFilters() {
        int checked = chipGroupTriage.getCheckedChipId();
        String level = checked == R.id.chipCasesCritical ? Triage.CRITICAL
                : checked == R.id.chipCasesUrgent ? Triage.URGENT
                : checked == R.id.chipCasesStable ? Triage.STABLE
                : null;

        List<TreatedCase> result = new ArrayList<>();
        for (TreatedCase c : allCases) {
            if (level != null && !level.equals(c.getTriageLevel())) continue;
            if (!query.isEmpty()
                    && !c.getDescription().contains(query)
                    && !c.getProceduralNotes().contains(query)
                    && !String.valueOf(c.getLogId()).equals(query.replace("#", ""))) {
                continue;
            }
            result.add(c);
        }
        adapter.submitList(result);

        boolean isEmpty = result.isEmpty();
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvCases.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}

package com.svu.firstaidhero.views.fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.svu.firstaidhero.R;
import com.svu.firstaidhero.database.entities.Emergency;
import com.svu.firstaidhero.utils.TextSanitizer;
import com.svu.firstaidhero.viewmodels.EmergencyViewModel;
import com.svu.firstaidhero.views.activities.EmergencyDetailActivity;
import com.svu.firstaidhero.views.adapters.EmergencyAdapter;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Home tab: searchable, filterable, sortable list of first-aid protocols. */
public class GuidesFragment extends Fragment {

    private EmergencyViewModel viewModel;
    private EmergencyAdapter adapter;

    private TextInputEditText etSearch;
    private ChipGroup chipGroup;
    private MaterialButtonToggleGroup toggleSort;
    private RecyclerView rvEmergencies;
    private TextView tvEmpty;

    // The LiveData we are currently observing (all or search results)
    private LiveData<List<Emergency>> currentSource;
    // The last list received from the database, before chip filtering
    private List<Emergency> latestList = new ArrayList<>();

    private final Observer<List<Emergency>> emergencyObserver = list -> {
        latestList = (list != null) ? list : new ArrayList<>();
        applyFilter();
    };

    private final ActivityResultLauncher<Intent> voiceSearch =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) return;
                ArrayList<String> matches =
                        result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (matches != null && !matches.isEmpty()) {
                    etSearch.setText(matches.get(0));
                    etSearch.setSelection(etSearch.length());
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_guides, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(EmergencyViewModel.class);

        TextInputLayout tilSearch = view.findViewById(R.id.tilSearch);
        etSearch = view.findViewById(R.id.etSearch);
        chipGroup = view.findViewById(R.id.chipGroup);
        toggleSort = view.findViewById(R.id.toggleSort);
        rvEmergencies = view.findViewById(R.id.rvEmergencies);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        adapter = new EmergencyAdapter();
        adapter.setOnEmergencyClickListener(emergency -> {
            Intent intent = new Intent(requireContext(), EmergencyDetailActivity.class);
            intent.putExtra(EmergencyDetailActivity.EXTRA_CASE_ID, emergency.getCaseId());
            startActivity(intent);
        });
        rvEmergencies.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvEmergencies.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                observeEmergencies(s.toString().trim());
            }
        });

        tilSearch.setEndIconOnClickListener(v -> startVoiceSearch());
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> applyFilter());
        toggleSort.addOnButtonCheckedListener((group, checkedId, isChecked) -> applyFilter());

        observeEmergencies("");
    }

    private void startVoiceSearch() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_search_prompt));
        try {
            voiceSearch.launch(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext(), R.string.voice_not_available, Toast.LENGTH_SHORT).show();
        }
    }

    /** Switches between "all" and "search" LiveData, removing the old observer first. */
    private void observeEmergencies(String query) {
        if (currentSource != null) {
            currentSource.removeObserver(emergencyObserver);
        }
        currentSource = query.isEmpty()
                ? viewModel.getAllEmergencies()
                : viewModel.searchEmergencies(query);
        currentSource.observe(getViewLifecycleOwner(), emergencyObserver);
    }

    /** Applies the selected chip and sort on top of the latest database result. */
    private void applyFilter() {
        int checkedId = chipGroup.getCheckedChipId();
        List<Emergency> result = new ArrayList<>();
        int highCount = 0;
        int commonCount = 0;

        for (Emergency e : latestList) {
            boolean high = "High".equalsIgnoreCase(e.getDangerLevel());
            if (high) highCount++;
            if (e.isCommon()) commonCount++;

            if (checkedId == R.id.chipCommon) {
                if (e.isCommon()) result.add(e);
            } else if (checkedId == R.id.chipHigh) {
                if (high) result.add(e);
            } else {
                result.add(e);
            }
        }

        setChipCount(R.id.chipAll, R.string.filter_all, latestList.size());
        setChipCount(R.id.chipHigh, R.string.triage_short_critical, highCount);
        setChipCount(R.id.chipCommon, R.string.filter_common_short, commonCount);

        int sortId = toggleSort.getCheckedButtonId();
        if (sortId == R.id.btnSortSevere) {
            // Stable sort: critical first, otherwise keep the database order
            Collections.sort(result, (a, b) -> Boolean.compare(
                    !"High".equalsIgnoreCase(a.getDangerLevel()),
                    !"High".equalsIgnoreCase(b.getDangerLevel())));
        } else if (sortId == R.id.btnSortAlpha) {
            Collator collator = Collator.getInstance(new Locale("ar"));
            Collections.sort(result, (a, b) -> collator.compare(
                    TextSanitizer.cleanTitle(a.getTitle()), TextSanitizer.cleanTitle(b.getTitle())));
        }

        adapter.submitList(result);

        boolean isEmpty = result.isEmpty();
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvEmergencies.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void setChipCount(int chipId, int labelRes, int count) {
        Chip chip = chipGroup.findViewById(chipId);
        chip.setText(getString(R.string.filter_count, getString(labelRes), count));
    }
}

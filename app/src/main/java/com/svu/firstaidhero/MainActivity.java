package com.svu.firstaidhero;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.svu.firstaidhero.database.entities.Emergency;
import com.svu.firstaidhero.viewmodels.EmergencyViewModel;
import com.svu.firstaidhero.views.activities.EmergencyDetailActivity;
import com.svu.firstaidhero.views.adapters.EmergencyAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EmergencyViewModel viewModel;
    private EmergencyAdapter adapter;

    private TextInputEditText etSearch;
    private ChipGroup chipGroup;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        viewModel = new ViewModelProvider(this).get(EmergencyViewModel.class);

        etSearch = findViewById(R.id.etSearch);
        chipGroup = findViewById(R.id.chipGroup);
        rvEmergencies = findViewById(R.id.rvEmergencies);
        tvEmpty = findViewById(R.id.tvEmpty);

        setupRecyclerView();
        setupSearch();
        setupChips();

        observeEmergencies("");
    }

    private void setupRecyclerView() {
        adapter = new EmergencyAdapter();
        adapter.setOnEmergencyClickListener(emergency -> {
            Intent intent = new Intent(this, EmergencyDetailActivity.class);
            intent.putExtra(EmergencyDetailActivity.EXTRA_CASE_ID, emergency.getCaseId());
            startActivity(intent);
        });
        rvEmergencies.setLayoutManager(new LinearLayoutManager(this));
        rvEmergencies.setAdapter(adapter);
    }

    private void setupSearch() {
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
    }

    private void setupChips() {
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> applyFilter());
    }

    /** Switches between "all" and "search" LiveData, removing the old observer first. */
    private void observeEmergencies(String query) {
        if (currentSource != null) {
            currentSource.removeObserver(emergencyObserver);
        }
        currentSource = query.isEmpty()
                ? viewModel.getAllEmergencies()
                : viewModel.searchEmergencies(query);
        currentSource.observe(this, emergencyObserver);
    }

    /** Applies the selected chip on top of the latest database result. */
    private void applyFilter() {
        int checkedId = chipGroup.getCheckedChipId();
        List<Emergency> result = new ArrayList<>();

        for (Emergency e : latestList) {
            if (checkedId == R.id.chipCommon) {
                if (e.isCommon()) result.add(e);
            } else if (checkedId == R.id.chipHigh) {
                if ("High".equalsIgnoreCase(e.getDangerLevel())) result.add(e);
            } else {
                result.add(e);
            }
        }

        adapter.submitList(result);

        boolean isEmpty = result.isEmpty();
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvEmergencies.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
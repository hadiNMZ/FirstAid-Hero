package com.svu.firstaidhero.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.svu.firstaidhero.database.entities.Emergency;
import com.svu.firstaidhero.database.entities.Step;
import com.svu.firstaidhero.repository.FirstAidRepository;

import java.util.List;

public class EmergencyViewModel extends AndroidViewModel {

    private final FirstAidRepository repository;

    public EmergencyViewModel(@NonNull Application application) {
        super(application);
        this.repository = new FirstAidRepository(application);
    }

    public LiveData<List<Emergency>> getAllEmergencies() {
        return repository.getAllEmergencies();
    }

    public LiveData<List<Emergency>> searchEmergencies(String query) {
        return repository.searchEmergencies(query);
    }

    public LiveData<Emergency> getEmergencyById(int caseId) {
        return repository.getEmergencyById(caseId);
    }

    public LiveData<List<Step>> getStepsForEmergency(int caseId) {
        return repository.getStepsForEmergency(caseId);
    }
}

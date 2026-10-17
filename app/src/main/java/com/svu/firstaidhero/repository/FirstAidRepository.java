package com.svu.firstaidhero.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.svu.firstaidhero.database.AppDatabase;
import com.svu.firstaidhero.database.dao.CustomContactDao;
import com.svu.firstaidhero.database.dao.EmergencyDao;
import com.svu.firstaidhero.database.dao.StepDao;
import com.svu.firstaidhero.database.dao.TreatedCaseDao;
import com.svu.firstaidhero.database.entities.CustomContact;
import com.svu.firstaidhero.database.entities.Emergency;
import com.svu.firstaidhero.database.entities.Step;
import com.svu.firstaidhero.database.entities.TreatedCase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FirstAidRepository {

    private final EmergencyDao emergencyDao;
    private final StepDao stepDao;
    private final CustomContactDao customContactDao;
    private final TreatedCaseDao treatedCaseDao;

    private final ExecutorService executorService;

    public FirstAidRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.emergencyDao = db.emergencyDao();
        this.stepDao = db.stepDao();
        this.customContactDao = db.customContactDao();
        this.treatedCaseDao = db.treatedCaseDao();
        this.executorService = Executors.newFixedThreadPool(4);
    }

    // Emergency Operations
    public LiveData<List<Emergency>> getAllEmergencies() {
        MutableLiveData<List<Emergency>> data = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Emergency> result = emergencyDao.getAllEmergencies();
            data.postValue(result);
        });
        return data;
    }

    public LiveData<List<Emergency>> searchEmergencies(String query) {
        MutableLiveData<List<Emergency>> data = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Emergency> result = emergencyDao.searchEmergencies(query);
            data.postValue(result);
        });
        return data;
    }

    public LiveData<Emergency> getEmergencyById(int caseId) {
        MutableLiveData<Emergency> data = new MutableLiveData<>();
        executorService.execute(() -> {
            Emergency result = emergencyDao.getEmergencyById(caseId);
            data.postValue(result);
        });
        return data;
    }

    // Step Operations
    public LiveData<List<Step>> getStepsForEmergency(int caseId) {
        MutableLiveData<List<Step>> data = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Step> result = stepDao.getStepsForEmergency(caseId);
            data.postValue(result);
        });
        return data;
    }

    // Contact Operations
    public LiveData<List<CustomContact>> getAllContacts() {
        MutableLiveData<List<CustomContact>> data = new MutableLiveData<>();
        executorService.execute(() -> {
            List<CustomContact> result = customContactDao.getAllContacts();
            data.postValue(result);
        });
        return data;
    }

    public void insertContact(CustomContact contact) {
        executorService.execute(() -> customContactDao.insertContact(contact));
    }

    // Treated Cases Operations
    public LiveData<List<TreatedCase>> getAllTreatedCases() {
        MutableLiveData<List<TreatedCase>> data = new MutableLiveData<>();
        executorService.execute(() -> {
            List<TreatedCase> result = treatedCaseDao.getAllTreatedCases();
            data.postValue(result);
        });
        return data;
    }

    public void insertTreatedCase(TreatedCase treatedCase) {
        executorService.execute(() -> treatedCaseDao.insertTreatedCase(treatedCase));
    }
}

package com.svu.firstaidhero.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.svu.firstaidhero.database.AppDatabase;
import com.svu.firstaidhero.database.dao.CustomContactDao;
import com.svu.firstaidhero.database.dao.EmergencyDao;
import com.svu.firstaidhero.database.dao.KitItemDao;
import com.svu.firstaidhero.database.dao.StepDao;
import com.svu.firstaidhero.database.dao.TreatedCaseDao;
import com.svu.firstaidhero.database.entities.CustomContact;
import com.svu.firstaidhero.database.entities.Emergency;
import com.svu.firstaidhero.database.entities.KitItem;
import com.svu.firstaidhero.database.entities.Step;
import com.svu.firstaidhero.database.entities.TreatedCase;
import com.svu.firstaidhero.utils.TextSanitizer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FirstAidRepository {

    private final EmergencyDao emergencyDao;
    private final StepDao stepDao;
    private final CustomContactDao customContactDao;
    private final TreatedCaseDao treatedCaseDao;
    private final KitItemDao kitItemDao;

    private final ExecutorService executorService;

    public FirstAidRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.emergencyDao = db.emergencyDao();
        this.stepDao = db.stepDao();
        this.customContactDao = db.customContactDao();
        this.treatedCaseDao = db.treatedCaseDao();
        this.kitItemDao = db.kitItemDao();
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
            List<Emergency> all = emergencyDao.getAllEmergencies();
            if (query == null || query.trim().isEmpty()) {
                data.postValue(all);
                return;
            }

            String normalizedQuery = TextSanitizer.normalizeArabic(query);
            List<Emergency> filtered = new ArrayList<>();

            for (Emergency e : all) {
                if (e == null) continue;
                String normalizedTarget = TextSanitizer.normalizeArabic(
                        e.getTitle() + " " + e.getSymptoms() + " " + e.getKeywords()
                );

                if (normalizedTarget.contains(normalizedQuery)) {
                    filtered.add(e);
                }
            }

            // Fallback to database query if needed
            if (filtered.isEmpty()) {
                filtered = emergencyDao.searchEmergencies(query);
            }

            data.postValue(filtered);
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
        return customContactDao.getAllContacts();
    }

    public LiveData<CustomContact> getContactById(int contactId) {
        return customContactDao.getContactById(contactId);
    }

    public void insertContact(CustomContact contact) {
        executorService.execute(() -> customContactDao.insertContact(contact));
    }

    public void updateContact(CustomContact contact) {
        executorService.execute(() -> customContactDao.updateContact(contact));
    }

    public void deleteContact(CustomContact contact) {
        executorService.execute(() -> customContactDao.deleteContact(contact));
    }

    // Treated Cases Operations
    public LiveData<List<TreatedCase>> getAllTreatedCases() {
        return treatedCaseDao.getAllTreatedCases();
    }

    public LiveData<TreatedCase> getTreatedCaseById(int logId) {
        return treatedCaseDao.getTreatedCaseById(logId);
    }

    public void insertTreatedCase(TreatedCase treatedCase) {
        executorService.execute(() -> treatedCaseDao.insertTreatedCase(treatedCase));
    }

    public void updateTreatedCase(TreatedCase treatedCase) {
        executorService.execute(() -> treatedCaseDao.updateTreatedCase(treatedCase));
    }

    public void deleteTreatedCase(TreatedCase treatedCase) {
        executorService.execute(() -> treatedCaseDao.deleteTreatedCase(treatedCase));
    }

    // Kit Operations
    public LiveData<List<KitItem>> getAllKitItems() {
        return kitItemDao.getAllItems();
    }

    public void updateKitItem(KitItem item) {
        executorService.execute(() -> kitItemDao.updateItem(item));
    }
}

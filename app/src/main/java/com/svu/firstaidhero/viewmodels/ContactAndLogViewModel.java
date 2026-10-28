package com.svu.firstaidhero.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.svu.firstaidhero.database.entities.CustomContact;
import com.svu.firstaidhero.database.entities.TreatedCase;
import com.svu.firstaidhero.repository.FirstAidRepository;

import java.util.List;

public class ContactAndLogViewModel extends AndroidViewModel {

    private final FirstAidRepository repository;

    public ContactAndLogViewModel(@NonNull Application application) {
        super(application);
        this.repository = new FirstAidRepository(application);
    }

    public LiveData<List<CustomContact>> getAllContacts() {
        return repository.getAllContacts();
    }

    public LiveData<CustomContact> getContactById(int contactId) {
        return repository.getContactById(contactId);
    }

    public void insertContact(CustomContact contact) {
        repository.insertContact(contact);
    }

    public void updateContact(CustomContact contact) {
        repository.updateContact(contact);
    }

    public void deleteContact(CustomContact contact) {
        repository.deleteContact(contact);
    }

    public LiveData<List<TreatedCase>> getAllTreatedCases() {
        return repository.getAllTreatedCases();
    }

    public LiveData<TreatedCase> getTreatedCaseById(int logId) {
        return repository.getTreatedCaseById(logId);
    }

    public void insertTreatedCase(TreatedCase treatedCase) {
        repository.insertTreatedCase(treatedCase);
    }

    public void updateTreatedCase(TreatedCase treatedCase) {
        repository.updateTreatedCase(treatedCase);
    }

    public void deleteTreatedCase(TreatedCase treatedCase) {
        repository.deleteTreatedCase(treatedCase);
    }
}

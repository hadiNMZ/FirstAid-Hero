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

    public void insertContact(CustomContact contact) {
        repository.insertContact(contact);
    }

    public LiveData<List<TreatedCase>> getAllTreatedCases() {
        return repository.getAllTreatedCases();
    }

    public void insertTreatedCase(TreatedCase treatedCase) {
        repository.insertTreatedCase(treatedCase);
    }
}

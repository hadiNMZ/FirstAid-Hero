package com.svu.firstaidhero.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.svu.firstaidhero.database.entities.KitItem;
import com.svu.firstaidhero.repository.FirstAidRepository;

import java.util.List;

public class KitViewModel extends AndroidViewModel {

    private final FirstAidRepository repository;

    public KitViewModel(@NonNull Application application) {
        super(application);
        this.repository = new FirstAidRepository(application);
    }

    public LiveData<List<KitItem>> getAllItems() {
        return repository.getAllKitItems();
    }

    public void updateItem(KitItem item) {
        repository.updateKitItem(item);
    }
}

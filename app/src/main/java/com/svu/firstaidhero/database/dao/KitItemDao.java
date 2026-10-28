package com.svu.firstaidhero.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Update;

import com.svu.firstaidhero.database.entities.KitItem;

import java.util.List;

@Dao
public interface KitItemDao {

    @Query("SELECT * FROM kit_items ORDER BY item_id")
    LiveData<List<KitItem>> getAllItems();

    @Update
    void updateItem(KitItem item);
}

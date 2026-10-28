package com.svu.firstaidhero.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.svu.firstaidhero.database.entities.CustomContact;

import java.util.List;

@Dao
public interface CustomContactDao {

    // LiveData so the list refreshes by itself after an add, edit or delete
    @Query("SELECT * FROM custom_contacts ORDER BY name")
    LiveData<List<CustomContact>> getAllContacts();

    @Query("SELECT * FROM custom_contacts WHERE contact_id = :contactId")
    LiveData<CustomContact> getContactById(int contactId);

    @Insert
    void insertContact(CustomContact contact);

    @Update
    void updateContact(CustomContact contact);

    @Delete
    void deleteContact(CustomContact contact);
}

package com.svu.firstaidhero.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.svu.firstaidhero.database.entities.CustomContact;

import java.util.List;

@Dao
public interface CustomContactDao {

    @Query("SELECT * FROM custom_contacts")
    List<CustomContact> getAllContacts();

    @Insert
    void insertContact(CustomContact contact);
}

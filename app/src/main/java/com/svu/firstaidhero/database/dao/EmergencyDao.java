package com.svu.firstaidhero.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.svu.firstaidhero.database.entities.Emergency;

import java.util.List;

@Dao
public interface EmergencyDao {

    @Query("SELECT * FROM emergencies")
    List<Emergency> getAllEmergencies();

    @Query("SELECT * FROM emergencies WHERE title LIKE '%' || :query || '%' OR symptoms LIKE '%' || :query || '%' OR keywords LIKE '%' || :query || '%'")
    List<Emergency> searchEmergencies(String query);

    @Query("SELECT * FROM emergencies WHERE case_id = :caseId")
    Emergency getEmergencyById(int caseId);

    @Insert
    void insertEmergency(Emergency emergency);
}

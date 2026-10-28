package com.svu.firstaidhero.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.svu.firstaidhero.database.entities.TreatedCase;

import java.util.List;

@Dao
public interface TreatedCaseDao {

    // LiveData so a newly logged case shows up without restarting the app
    @Query("SELECT * FROM treated_cases ORDER BY log_id DESC")
    LiveData<List<TreatedCase>> getAllTreatedCases();

    @Query("SELECT * FROM treated_cases WHERE log_id = :logId")
    LiveData<TreatedCase> getTreatedCaseById(int logId);

    @Insert
    void insertTreatedCase(TreatedCase treatedCase);

    @Update
    void updateTreatedCase(TreatedCase treatedCase);

    @Delete
    void deleteTreatedCase(TreatedCase treatedCase);
}

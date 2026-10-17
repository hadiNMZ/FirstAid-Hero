package com.svu.firstaidhero.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.svu.firstaidhero.database.entities.TreatedCase;

import java.util.List;

@Dao
public interface TreatedCaseDao {

    @Query("SELECT * FROM treated_cases")
    List<TreatedCase> getAllTreatedCases();

    @Insert
    void insertTreatedCase(TreatedCase treatedCase);
}

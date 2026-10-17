package com.svu.firstaidhero.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.svu.firstaidhero.database.entities.Step;

import java.util.List;

@Dao
public interface StepDao {

    @Query("SELECT * FROM steps WHERE case_id = :caseId ORDER BY step_number ASC")
    List<Step> getStepsForEmergency(int caseId);

    @Insert
    void insertStep(Step step);
}

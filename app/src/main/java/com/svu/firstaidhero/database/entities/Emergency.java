package com.svu.firstaidhero.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "emergencies")
public class Emergency {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "case_id")
    private int caseId;

    @NonNull
    @ColumnInfo(name = "title")
    private String title = "";

    @NonNull
    @ColumnInfo(name = "symptoms")
    private String symptoms = "";

    @NonNull
    @ColumnInfo(name = "danger_level")
    private String dangerLevel = "";

    @NonNull
    @ColumnInfo(name = "keywords")
    private String keywords = "";

    @ColumnInfo(name = "is_common")
    private boolean isCommon;

    // Default empty constructor required by Room
    public Emergency() {
    }

    // Parameterized constructor ignoring auto-generated primary key
    @Ignore
    public Emergency(@NonNull String title, @NonNull String symptoms, @NonNull String dangerLevel, @NonNull String keywords, boolean isCommon) {
        this.title = title;
        this.symptoms = symptoms;
        this.dangerLevel = dangerLevel;
        this.keywords = keywords;
        this.isCommon = isCommon;
    }

    public int getCaseId() {
        return caseId;
    }

    public void setCaseId(int caseId) {
        this.caseId = caseId;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    @NonNull
    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(@NonNull String symptoms) {
        this.symptoms = symptoms;
    }

    @NonNull
    public String getDangerLevel() {
        return dangerLevel;
    }

    public void setDangerLevel(@NonNull String dangerLevel) {
        this.dangerLevel = dangerLevel;
    }

    @NonNull
    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(@NonNull String keywords) {
        this.keywords = keywords;
    }

    public boolean isCommon() {
        return isCommon;
    }

    public void setCommon(boolean isCommon) {
        this.isCommon = isCommon;
    }

    public void setIsCommon(boolean isCommon) {
        this.isCommon = isCommon;
    }
}

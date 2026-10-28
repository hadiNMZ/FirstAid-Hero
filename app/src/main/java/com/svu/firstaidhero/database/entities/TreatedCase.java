package com.svu.firstaidhero.database.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "treated_cases")
public class TreatedCase {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "log_id")
    private int logId;

    @NonNull
    @ColumnInfo(name = "description")
    private String description = "";

    @NonNull
    @ColumnInfo(name = "date_treated")
    private String dateTreated = "";

    @NonNull
    @ColumnInfo(name = "procedural_notes")
    private String proceduralNotes = "";

    @Nullable
    @ColumnInfo(name = "photo_uri")
    private String photoUri;

    // One of the Triage constants: stable, urgent or critical (added in database version 2)
    @NonNull
    @ColumnInfo(name = "triage_level", defaultValue = "stable")
    private String triageLevel = "stable";

    // Default empty constructor required by Room
    public TreatedCase() {
    }

    // Parameterized constructor ignoring auto-generated primary key
    @Ignore
    public TreatedCase(@NonNull String description, @NonNull String dateTreated, @NonNull String proceduralNotes, @Nullable String photoUri) {
        this.description = description;
        this.dateTreated = dateTreated;
        this.proceduralNotes = proceduralNotes;
        this.photoUri = photoUri;
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    public void setDescription(@NonNull String description) {
        this.description = description;
    }

    @NonNull
    public String getDateTreated() {
        return dateTreated;
    }

    public void setDateTreated(@NonNull String dateTreated) {
        this.dateTreated = dateTreated;
    }

    @NonNull
    public String getProceduralNotes() {
        return proceduralNotes;
    }

    public void setProceduralNotes(@NonNull String proceduralNotes) {
        this.proceduralNotes = proceduralNotes;
    }

    @Nullable
    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(@Nullable String photoUri) {
        this.photoUri = photoUri;
    }

    @NonNull
    public String getTriageLevel() {
        return triageLevel;
    }

    public void setTriageLevel(@NonNull String triageLevel) {
        this.triageLevel = triageLevel;
    }
}

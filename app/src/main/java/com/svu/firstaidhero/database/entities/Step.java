package com.svu.firstaidhero.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "steps",
    foreignKeys = @ForeignKey(
        entity = Emergency.class,
        parentColumns = "case_id",
        childColumns = "case_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("case_id")}
)
public class Step {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "step_id")
    private int stepId;

    @ColumnInfo(name = "case_id")
    private int caseId;

    @ColumnInfo(name = "step_number")
    private int stepNumber;

    @NonNull
    @ColumnInfo(name = "instruction_text")
    private String instructionText = "";

    // Default empty constructor required by Room
    public Step() {
    }

    // Parameterized constructor ignoring auto-generated primary key
    @Ignore
    public Step(int caseId, int stepNumber, @NonNull String instructionText) {
        this.caseId = caseId;
        this.stepNumber = stepNumber;
        this.instructionText = instructionText;
    }

    public int getStepId() {
        return stepId;
    }

    public void setStepId(int stepId) {
        this.stepId = stepId;
    }

    public int getCaseId() {
        return caseId;
    }

    public void setCaseId(int caseId) {
        this.caseId = caseId;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }

    @NonNull
    public String getInstructionText() {
        return instructionText;
    }

    public void setInstructionText(@NonNull String instructionText) {
        this.instructionText = instructionText;
    }
}

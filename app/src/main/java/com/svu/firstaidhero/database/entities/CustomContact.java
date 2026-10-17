package com.svu.firstaidhero.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_contacts")
public class CustomContact {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "contact_id")
    private int contactId;

    @NonNull
    @ColumnInfo(name = "name")
    private String name = "";

    @NonNull
    @ColumnInfo(name = "phone_number")
    private String phoneNumber = "";

    @NonNull
    @ColumnInfo(name = "relationship")
    private String relationship = "";

    // Default empty constructor required by Room
    public CustomContact() {
    }

    // Parameterized constructor ignoring auto-generated primary key
    @Ignore
    public CustomContact(@NonNull String name, @NonNull String phoneNumber, @NonNull String relationship) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.relationship = relationship;
    }

    public int getContactId() {
        return contactId;
    }

    public void setContactId(int contactId) {
        this.contactId = contactId;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(@NonNull String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @NonNull
    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(@NonNull String relationship) {
        this.relationship = relationship;
    }
}

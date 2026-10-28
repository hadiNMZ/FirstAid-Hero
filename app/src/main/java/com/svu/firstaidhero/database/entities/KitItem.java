package com.svu.firstaidhero.database.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** One item in the user's first-aid kit inventory. */
@Entity(tableName = "kit_items")
public class KitItem {

    public static final String CATEGORY_BLEEDING = "bleeding";
    public static final String CATEGORY_BURNS = "burns";
    public static final String CATEGORY_TOOLS = "tools";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "item_id")
    private int itemId;

    @NonNull
    @ColumnInfo(name = "name")
    private String name = "";

    @NonNull
    @ColumnInfo(name = "name_en")
    private String nameEn = "";

    @NonNull
    @ColumnInfo(name = "category")
    private String category = CATEGORY_TOOLS;

    @ColumnInfo(name = "quantity")
    private int quantity;

    @ColumnInfo(name = "required_quantity")
    private int requiredQuantity;

    // Unit word shown after the quantity, e.g. قطعة / لفة
    @NonNull
    @ColumnInfo(name = "unit")
    private String unit = "";

    // Expiry as "yyyy/MM", or null when the item does not expire
    @Nullable
    @ColumnInfo(name = "expiry")
    private String expiry;

    // Optional drawable name for the product photo (see ImageResources)
    @Nullable
    @ColumnInfo(name = "image_name")
    private String imageName;

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(@NonNull String nameEn) {
        this.nameEn = nameEn;
    }

    @NonNull
    public String getCategory() {
        return category;
    }

    public void setCategory(@NonNull String category) {
        this.category = category;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getRequiredQuantity() {
        return requiredQuantity;
    }

    public void setRequiredQuantity(int requiredQuantity) {
        this.requiredQuantity = requiredQuantity;
    }

    @NonNull
    public String getUnit() {
        return unit;
    }

    public void setUnit(@NonNull String unit) {
        this.unit = unit;
    }

    @Nullable
    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(@Nullable String expiry) {
        this.expiry = expiry;
    }

    @Nullable
    public String getImageName() {
        return imageName;
    }

    public void setImageName(@Nullable String imageName) {
        this.imageName = imageName;
    }
}

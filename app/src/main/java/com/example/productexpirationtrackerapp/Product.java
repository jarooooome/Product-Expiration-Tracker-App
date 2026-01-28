package com.example.productexpirationtrackerapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "products")
public class Product {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;

    @TypeConverters(DateConverter.class)
    private Date expiryDate;

    // Empty constructor (REQUIRED for Room)
    public Product() {}

    // Constructor for manual creation
    public Product(String name, String expiryDateStr) {
        this.name = name;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            this.expiryDate = sdf.parse(expiryDateStr);
        } catch (ParseException e) {
            this.expiryDate = new Date(); // Default to today if parsing fails
        }
    }

    // Constructor with Date object
    public Product(String name, Date expiryDate) {
        this.name = name;
        this.expiryDate = expiryDate;
    }

    // Getters and Setters (REQUIRED for Room)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    // Helper methods (keep your existing ones)
    public String getFormattedExpiryDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(expiryDate);
    }

    public String getDisplayText() {
        return name + " - Expires: " + getFormattedExpiryDate();
    }

    // For date string input
    public void setExpiryDateFromString(String expiryDateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            this.expiryDate = sdf.parse(expiryDateStr);
        } catch (ParseException e) {
            this.expiryDate = new Date();
        }
    }
}
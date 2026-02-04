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

    // Photo field (optional - store as byte array)
    private byte[] photo;

    // NEW: Additional product details
    private String category;
    private String quantity;
    private String notes;

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

    // Constructor with all fields
    public Product(String name, Date expiryDate, String category, String quantity, String notes) {
        this.name = name;
        this.expiryDate = expiryDate;
        this.category = category;
        this.quantity = quantity;
        this.notes = notes;
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

    // Photo getter and setter
    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    // NEW: Category getter and setter
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // NEW: Quantity getter and setter
    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    // NEW: Notes getter and setter
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Helper methods
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

    // Helper method to check if product has photo
    public boolean hasPhoto() {
        return photo != null && photo.length > 0;
    }

    // NEW: Helper method to check if category exists
    public boolean hasCategory() {
        return category != null && !category.trim().isEmpty();
    }

    // NEW: Helper method to check if quantity exists
    public boolean hasQuantity() {
        return quantity != null && !quantity.trim().isEmpty();
    }

    // NEW: Helper method to check if notes exist
    public boolean hasNotes() {
        return notes != null && !notes.trim().isEmpty();
    }
}
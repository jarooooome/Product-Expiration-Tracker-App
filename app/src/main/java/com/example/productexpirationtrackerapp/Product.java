package com.example.productexpirationtrackerapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

@Entity(tableName = "products")
@TypeConverters({DateConverter.class})
public class Product {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String brand;
    private String category;
    private int quantity = 1;
    private String storageLocation;
    private Date expiryDate;
    private String notes;
    private String photoPath;

    // Constructor for Room database
    public Product() {
    }

    // Constructor for creating new products
    public Product(String name, Date expiryDate) {
        this.name = name;
        this.expiryDate = expiryDate;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }

    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    // Helper methods
    public String getFormattedExpiryDate() {
        if (expiryDate == null) return "No date";
        return DateConverter.dateToTimestamp(expiryDate);
    }

    public String getDisplayText() {
        return name + " - Expires: " + getFormattedExpiryDate();
    }

    // Check if product has a photo
    public boolean hasPhoto() {
        return photoPath != null && !photoPath.isEmpty();
    }

    // Get photo as byte array (if you store photos as bytes)
    public byte[] getPhoto() {
        // Implement if you store photos as byte arrays
        return null;
    }
}

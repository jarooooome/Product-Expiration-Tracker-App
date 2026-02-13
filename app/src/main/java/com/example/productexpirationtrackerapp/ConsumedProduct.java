package com.example.productexpirationtrackerapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "consumed_products")
public class ConsumedProduct {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int originalProductId; // ID from products table
    private String productName;
    private String category;
    private String quantity;
    private Date expiryDate;
    private Date actionDate; // When it was consumed/discarded
    private String actionType; // "CONSUMED" or "DISCARDED"
    private byte[] photo; // Optional: save photo for history

    // Constructor
    public ConsumedProduct(int originalProductId, String productName, String category,
                           String quantity, Date expiryDate, String actionType, byte[] photo) {
        this.originalProductId = originalProductId;
        this.productName = productName;
        this.category = category;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.actionDate = new Date(); // Current date/time
        this.actionType = actionType;
        this.photo = photo;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOriginalProductId() { return originalProductId; }
    public void setOriginalProductId(int originalProductId) { this.originalProductId = originalProductId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }

    public Date getActionDate() { return actionDate; }
    public void setActionDate(Date actionDate) { this.actionDate = actionDate; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public byte[] getPhoto() { return photo; }
    public void setPhoto(byte[] photo) { this.photo = photo; }

    // Helper method to check if has photo
    public boolean hasPhoto() {
        return photo != null && photo.length > 0;
    }
}
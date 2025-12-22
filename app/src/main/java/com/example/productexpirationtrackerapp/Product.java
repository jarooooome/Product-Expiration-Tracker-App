package com.example.productexpirationtrackerapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Product {
    private String name;
    private Date expiryDate;

    public Product(String name, String expiryDateStr) {
        this.name = name;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            this.expiryDate = sdf.parse(expiryDateStr);
        } catch (ParseException e) {
            this.expiryDate = new Date(); // Default to today if parsing fails
        }
    }

    public String getName() {
        return name;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public String getFormattedExpiryDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(expiryDate);
    }

    public String getDisplayText() {
        return name + " - Expires: " + getFormattedExpiryDate();
    }
}
package com.example.productexpirationtrackerapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageButton backButton;
    private ImageButton searchButton;
    private ImageButton editButton;
    private TextView productName;
    private TextView allTab;
    private TextView medicineTab;
    private TextView expiryStatus;
    private TextView quantityValue;
    private TextView categoryLabel;
    private TextView locationValue;
    private TextView storageLabel;
    private TextView itemsExpiringText;
    private BottomNavigationView bottomNavigation;

    private AppDatabase database;
    private Product currentProduct;
    private int productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Get product ID from intent
        productId = getIntent().getIntExtra("PRODUCT_ID", -1);
        if (productId == -1) {
            Toast.makeText(this, getString(R.string.error_product_not_found), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupDatabase();
        setupButtons();
        setupBottomNavigation();
        loadProductDetails();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        searchButton = findViewById(R.id.searchButton);
        editButton = findViewById(R.id.editButton);
        productName = findViewById(R.id.productName);
        allTab = findViewById(R.id.allTab);
        medicineTab = findViewById(R.id.medicineTab);
        expiryStatus = findViewById(R.id.expiryStatus);
        quantityValue = findViewById(R.id.quantityValue);
        categoryLabel = findViewById(R.id.categoryLabel);
        locationValue = findViewById(R.id.locationValue);
        storageLabel = findViewById(R.id.storageLabel);
        itemsExpiringText = findViewById(R.id.itemsExpiringText);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupDatabase() {
        database = AppDatabase.getDatabase(this);
    }

    private void setupButtons() {
        backButton.setOnClickListener(v -> finish());

        searchButton.setOnClickListener(v ->
                Toast.makeText(this, getString(R.string.search_coming_soon), Toast.LENGTH_SHORT).show()
        );

        editButton.setOnClickListener(v ->
                Toast.makeText(this, getString(R.string.edit_coming_soon), Toast.LENGTH_SHORT).show()
        );

        // Tab listeners
        allTab.setOnClickListener(v -> selectTab(allTab, medicineTab));
        medicineTab.setOnClickListener(v -> selectTab(medicineTab, allTab));
    }

    private void selectTab(TextView selectedTab, TextView otherTab) {
        // Visual feedback for selected tab
        selectedTab.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light));
        selectedTab.setBackgroundResource(R.drawable.tab_selected);

        otherTab.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        otherTab.setBackground(null);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(ProductDetailActivity.this, HomeActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_inventory) {
                startActivity(new Intent(ProductDetailActivity.this, InventoryActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(ProductDetailActivity.this, SettingsActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }

    private void loadProductDetails() {
        new Thread(() -> {
            try {
                currentProduct = database.productDao().getProductById(productId);

                if (currentProduct == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, getString(R.string.product_not_found), Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }

                runOnUiThread(this::displayProductDetails);

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, getString(R.string.error_loading_product_details, e.getMessage()),
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }).start();
    }

    private void displayProductDetails() {
        // Product name
        productName.setText(currentProduct.getName());

        // Quantity
        quantityValue.setText(String.valueOf(currentProduct.getQuantity()));

        // Category
        if (currentProduct.getCategory() != null && !currentProduct.getCategory().isEmpty()) {
            categoryLabel.setText(currentProduct.getCategory());
        } else {
            categoryLabel.setText(getString(R.string.uncategorized));
        }

        // Storage location
        if (currentProduct.getStorageLocation() != null && !currentProduct.getStorageLocation().isEmpty()) {
            locationValue.setText(currentProduct.getStorageLocation());
            storageLabel.setText(currentProduct.getCategory() != null ? currentProduct.getCategory() : "");
        } else {
            locationValue.setText(getString(R.string.not_specified));
            storageLabel.setText("");
        }

        // Expiry status
        long daysUntilExpiry = calculateDaysUntilExpiry(currentProduct.getExpiryDate());
        updateExpiryStatus(daysUntilExpiry);

        // Items expiring this week (placeholder - would need to query database)
        itemsExpiringText.setVisibility(View.GONE);
    }

    private long calculateDaysUntilExpiry(Date expiryDate) {
        if (expiryDate == null) {
            return 999;
        }

        try {
            Date today = new Date();

            // Reset time portion for both dates
            Date todayReset = resetTime(today);
            Date expiryDateReset = resetTime(expiryDate);

            long diffInMillis = expiryDateReset.getTime() - todayReset.getTime();
            return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            // Use Log instead of printStackTrace
            android.util.Log.e("ProductDetail", "Error calculating days until expiry", e);
            return 999;
        }
    }

    private Date resetTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private void updateExpiryStatus(long daysUntilExpiry) {
        int redColor = ContextCompat.getColor(this, android.R.color.holo_red_light);
        int orangeColor = ContextCompat.getColor(this, android.R.color.holo_orange_light);
        int greenColor = ContextCompat.getColor(this, android.R.color.holo_green_light);

        if (daysUntilExpiry < 0) {
            expiryStatus.setText(getString(R.string.expired_days_ago, Math.abs(daysUntilExpiry)));
            expiryStatus.setTextColor(redColor);
        } else if (daysUntilExpiry == 0) {
            // Check if you have the string resource, if not use a fallback
            String expiresTodayText = "Expires today";
            try {
                expiresTodayText = getString(R.string.expires_today);
            } catch (android.content.res.Resources.NotFoundException e) {
                // Resource not found, use fallback text
            }
            expiryStatus.setText(expiresTodayText);
            expiryStatus.setTextColor(orangeColor);
        } else if (daysUntilExpiry == 1) {
            expiryStatus.setText(getString(R.string.expires_tomorrow));
            expiryStatus.setTextColor(orangeColor);
        } else if (daysUntilExpiry <= 7) {
            expiryStatus.setText(getString(R.string.expires_in_days, daysUntilExpiry));
            expiryStatus.setTextColor(orangeColor);
        } else {
            expiryStatus.setText(getString(R.string.expires_in_days, daysUntilExpiry));
            expiryStatus.setTextColor(greenColor);
        }
    }
}
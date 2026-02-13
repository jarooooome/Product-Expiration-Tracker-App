package com.example.productexpirationtrackerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    // Header Views
    private TextView profileTitle;
    private TextView profileSubtitle;

    // Activity Stat Cards
    private CardView totalItemsCard;
    private CardView expiringSoonCard;
    private CardView expiredCard;

    // Category Cards
    private CardView fruitsCard;
    private CardView medicineCard;
    private CardView drinksCard;
    private CardView otherCard;

    // Category Item Count TextViews
    private TextView fruitsCount;
    private TextView medicineCount;
    private TextView drinksCount;
    private TextView otherCount;

    // Stat Count TextViews
    private TextView totalItemsCount;
    private TextView expiringSoonCount;
    private TextView expiredCount;

    // Floating Action Button
    private ImageView addButton;

    // Bottom Navigation
    private LinearLayout navProfile;
    private LinearLayout navProducts;
    private LinearLayout navSettings;
    private ImageView navProfileIcon;
    private ImageView navProductsIcon;
    private ImageView navSettingsIcon;
    private TextView navProfileText;
    private TextView navProductsText;
    private TextView navSettingsText;
    private View navProfileIndicator;
    private View navProductsIndicator;
    private View navSettingsIndicator;

    private ScrollView scrollView;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize preferences
        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        // Initialize all views
        initializeViews();

        // Setup bottom navigation
        setupBottomNavigation();

        // Setup click listeners
        setupClickListeners();

        // Apply theme
        applyTheme();

        // Load sample data (replace with actual data from database)
        loadProfileData();

        Log.d(TAG, "ProfileActivity created successfully");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reapply theme when returning to this activity
        applyTheme();
    }

    private void initializeViews() {
        try {
            // Header
            profileTitle = findViewById(R.id.profileTitle);
            profileSubtitle = findViewById(R.id.profileSubtitle);

            // Activity Stat Cards
            totalItemsCard = findViewById(R.id.total_items_card);
            expiringSoonCard = findViewById(R.id.expiring_soon_card);
            expiredCard = findViewById(R.id.expired_card);

            // Stat Count TextViews
            totalItemsCount = findViewById(R.id.total_items_count);
            expiringSoonCount = findViewById(R.id.expiring_soon_count);
            expiredCount = findViewById(R.id.expired_count);

            // Category Cards
            fruitsCard = findViewById(R.id.fruits_card);
            medicineCard = findViewById(R.id.medicine_card);
            drinksCard = findViewById(R.id.drinks_card);
            otherCard = findViewById(R.id.other_card);

            // Category Count TextViews
            fruitsCount = findViewById(R.id.fruits_count);
            medicineCount = findViewById(R.id.medicine_count);
            drinksCount = findViewById(R.id.drinks_count);
            otherCount = findViewById(R.id.other_count);

            // Floating Action Button
            addButton = findViewById(R.id.addButton);

            // Bottom Navigation
            navProfile = findViewById(R.id.navProfile);
            navProducts = findViewById(R.id.navProducts);
            navSettings = findViewById(R.id.navSettings);

            navProfileIcon = findViewById(R.id.navProfileIcon);
            navProductsIcon = findViewById(R.id.navProductsIcon);
            navSettingsIcon = findViewById(R.id.navSettingsIcon);

            navProfileText = findViewById(R.id.navProfileText);
            navProductsText = findViewById(R.id.navProductsText);
            navSettingsText = findViewById(R.id.navSettingsText);

            navProfileIndicator = findViewById(R.id.navProfileIndicator);
            navProductsIndicator = findViewById(R.id.navProductsIndicator);
            navSettingsIndicator = findViewById(R.id.navSettingsIndicator);

            scrollView = findViewById(R.id.scrollView);

            Log.d(TAG, "All views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
        }
    }

    private void setupBottomNavigation() {
        // Set Profile as active
        setActiveNavItem(navProfile, navProfileIcon, navProfileText, navProfileIndicator);

        // Set Products and Settings as inactive
        setInactiveNavItem(navProducts, navProductsIcon, navProductsText, navProductsIndicator);
        setInactiveNavItem(navSettings, navSettingsIcon, navSettingsText, navSettingsIndicator);
    }

    private void setActiveNavItem(LinearLayout navItem, ImageView icon, TextView text, View indicator) {
        if (navItem != null) {
            navItem.setBackgroundTintList(null);
        }
        if (icon != null) {
            icon.setImageTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
        }
        if (text != null) {
            text.setTextColor(Color.WHITE);
            text.setTextSize(12);
            text.setAlpha(1.0f);
        }
        if (indicator != null) {
            indicator.setVisibility(View.VISIBLE);
        }
    }

    private void setInactiveNavItem(LinearLayout navItem, ImageView icon, TextView text, View indicator) {
        if (icon != null) {
            icon.setImageTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
        }
        if (text != null) {
            text.setTextColor(Color.WHITE);
            text.setAlpha(0.8f);
            text.setTextSize(12);
        }
        if (indicator != null) {
            indicator.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        // Activity stat cards
        if (totalItemsCard != null) {
            totalItemsCard.setOnClickListener(v ->
                    Toast.makeText(ProfileActivity.this, "Total Items: 247", Toast.LENGTH_SHORT).show()
            );
        }

        if (expiringSoonCard != null) {
            expiringSoonCard.setOnClickListener(v ->
                    Toast.makeText(ProfileActivity.this, "Expiring Soon: 12 items", Toast.LENGTH_SHORT).show()
            );
        }

        if (expiredCard != null) {
            expiredCard.setOnClickListener(v ->
                    Toast.makeText(ProfileActivity.this, "Expired: 3 items", Toast.LENGTH_SHORT).show()
            );
        }

        // Category cards
        if (fruitsCard != null) {
            fruitsCard.setOnClickListener(v ->
                    Toast.makeText(ProfileActivity.this, "Fruits & Vegetables: 124 items", Toast.LENGTH_SHORT).show()
            );
        }

        if (medicineCard != null) {
            medicineCard.setOnClickListener(v ->
                    Toast.makeText(ProfileActivity.this, "Medicine: 45 items", Toast.LENGTH_SHORT).show()
            );
        }

        if (drinksCard != null) {
            drinksCard.setOnClickListener(v ->
                    Toast.makeText(ProfileActivity.this, "Drinks: 38 items", Toast.LENGTH_SHORT).show()
            );
        }

        if (otherCard != null) {
            otherCard.setOnClickListener(v ->
                    Toast.makeText(ProfileActivity.this, "Other: 40 items", Toast.LENGTH_SHORT).show()
            );
        }

        // Floating Action Button - Add Product
        if (addButton != null) {
            addButton.setOnClickListener(v -> {
                Log.d(TAG, "Add button clicked - opening AddProductActivity");
                Intent intent = new Intent(ProfileActivity.this, AddProductActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        // Bottom Navigation
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Log.d(TAG, "Profile navigation clicked - already on this screen");
                if (scrollView != null) {
                    scrollView.smoothScrollTo(0, 0);
                }
            });
        }

        if (navProducts != null) {
            navProducts.setOnClickListener(v -> {
                Log.d(TAG, "Products navigation clicked");
                Intent intent = new Intent(ProfileActivity.this, ProductListActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            });
        }

        if (navSettings != null) {
            navSettings.setOnClickListener(v -> {
                Log.d(TAG, "Settings navigation clicked");
                Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }
    }

    private void applyTheme() {
        // Get theme color from preferences
        int fabBackgroundColor = preferences.getInt("fab_background_color",
                getResources().getColor(R.color.color_fab_white));

        // Apply to bottom navigation
        LinearLayout bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            bottomNavigation.setBackgroundColor(fabBackgroundColor);
        }

        // Apply to FAB
        if (addButton != null) {
            addButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(fabBackgroundColor));
            addButton.setImageTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
        }

        // Bottom navigation icons and text colors
        int whiteColor = Color.WHITE;

        if (navProfileIcon != null) navProfileIcon.setImageTintList(android.content.res.ColorStateList.valueOf(whiteColor));
        if (navProductsIcon != null) navProductsIcon.setImageTintList(android.content.res.ColorStateList.valueOf(whiteColor));
        if (navSettingsIcon != null) navSettingsIcon.setImageTintList(android.content.res.ColorStateList.valueOf(whiteColor));

        if (navProfileText != null) navProfileText.setTextColor(whiteColor);
        if (navProductsText != null) navProductsText.setTextColor(whiteColor);
        if (navSettingsText != null) navSettingsText.setTextColor(whiteColor);

        if (navProfileIndicator != null) navProfileIndicator.setBackgroundColor(whiteColor);
        if (navProductsIndicator != null) navProductsIndicator.setBackgroundColor(whiteColor);
        if (navSettingsIndicator != null) navSettingsIndicator.setBackgroundColor(whiteColor);

        Log.d(TAG, "Theme applied to profile activity");
    }

    private void loadProfileData() {
        // Set stat counts
        if (totalItemsCount != null) totalItemsCount.setText("247");
        if (expiringSoonCount != null) expiringSoonCount.setText("12");
        if (expiredCount != null) expiredCount.setText("3");

        // Set category item counts
        if (fruitsCount != null) fruitsCount.setText("124 items");
        if (medicineCount != null) medicineCount.setText("45 items");
        if (drinksCount != null) drinksCount.setText("38 items");
        if (otherCount != null) otherCount.setText("40 items");

        Log.d(TAG, "Profile data loaded");
    }
}
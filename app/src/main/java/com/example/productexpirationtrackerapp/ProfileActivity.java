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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    // Header Views
    private TextView profileTitle;
    private TextView profileSubtitle;

    // Activity Stat Cards
    private CardView totalItemsCard;
    private CardView expiringSoonCard;
    private CardView expiredCard;

    // Category Cards - UPDATED with 7 categories
    private CardView dairyCard;
    private CardView vegetablesCard;
    private CardView fruitsCard;
    private CardView meatsCard;
    private CardView beveragesCard;
    private CardView medicineCard;
    private CardView otherCard;

    // Category Item Count TextViews - UPDATED with 7 categories
    private TextView dairyCount;
    private TextView vegetablesCount;
    private TextView fruitsCount;
    private TextView meatsCount;
    private TextView beveragesCount;
    private TextView medicineCount;
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
    private ProductViewModel productViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize ViewModel
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

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

        // Load real data from database
        loadProfileData();

        Log.d(TAG, "ProfileActivity created successfully");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reapply theme when returning to this activity
        applyTheme();
        // Refresh data when returning to profile
        loadProfileData();
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

            // Category Cards - UPDATED with 7 categories
            dairyCard = findViewById(R.id.dairy_card);
            vegetablesCard = findViewById(R.id.vegetables_card);
            fruitsCard = findViewById(R.id.fruits_card);
            meatsCard = findViewById(R.id.meats_card);
            beveragesCard = findViewById(R.id.beverages_card);
            medicineCard = findViewById(R.id.medicine_card);
            otherCard = findViewById(R.id.other_card);

            // Category Count TextViews - UPDATED with 7 categories
            dairyCount = findViewById(R.id.dairy_count);
            vegetablesCount = findViewById(R.id.vegetables_count);
            fruitsCount = findViewById(R.id.fruits_count);
            meatsCount = findViewById(R.id.meats_count);
            beveragesCount = findViewById(R.id.beverages_count);
            medicineCount = findViewById(R.id.medicine_count);
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
            totalItemsCard.setOnClickListener(v -> {
                String count = totalItemsCount != null ? totalItemsCount.getText().toString() : "0";
                Toast.makeText(ProfileActivity.this, "Total Items: " + count, Toast.LENGTH_SHORT).show();
            });
        }

        if (expiringSoonCard != null) {
            expiringSoonCard.setOnClickListener(v -> {
                String count = expiringSoonCount != null ? expiringSoonCount.getText().toString() : "0";
                Toast.makeText(ProfileActivity.this, "Expiring Soon: " + count + " items", Toast.LENGTH_SHORT).show();
            });
        }

        if (expiredCard != null) {
            expiredCard.setOnClickListener(v -> {
                String count = expiredCount != null ? expiredCount.getText().toString() : "0";
                Toast.makeText(ProfileActivity.this, "Expired: " + count + " items", Toast.LENGTH_SHORT).show();
            });
        }

        // Category cards - UPDATED with 7 categories
        if (dairyCard != null) {
            dairyCard.setOnClickListener(v -> {
                String count = dairyCount != null ? dairyCount.getText().toString() : "0 items";
                Toast.makeText(ProfileActivity.this, "Dairy: " + count, Toast.LENGTH_SHORT).show();
            });
        }

        if (vegetablesCard != null) {
            vegetablesCard.setOnClickListener(v -> {
                String count = vegetablesCount != null ? vegetablesCount.getText().toString() : "0 items";
                Toast.makeText(ProfileActivity.this, "Vegetables: " + count, Toast.LENGTH_SHORT).show();
            });
        }

        if (fruitsCard != null) {
            fruitsCard.setOnClickListener(v -> {
                String count = fruitsCount != null ? fruitsCount.getText().toString() : "0 items";
                Toast.makeText(ProfileActivity.this, "Fruits: " + count, Toast.LENGTH_SHORT).show();
            });
        }

        if (meatsCard != null) {
            meatsCard.setOnClickListener(v -> {
                String count = meatsCount != null ? meatsCount.getText().toString() : "0 items";
                Toast.makeText(ProfileActivity.this, "Meats: " + count, Toast.LENGTH_SHORT).show();
            });
        }

        if (beveragesCard != null) {
            beveragesCard.setOnClickListener(v -> {
                String count = beveragesCount != null ? beveragesCount.getText().toString() : "0 items";
                Toast.makeText(ProfileActivity.this, "Beverages: " + count, Toast.LENGTH_SHORT).show();
            });
        }

        if (medicineCard != null) {
            medicineCard.setOnClickListener(v -> {
                String count = medicineCount != null ? medicineCount.getText().toString() : "0 items";
                Toast.makeText(ProfileActivity.this, "Medicine: " + count, Toast.LENGTH_SHORT).show();
            });
        }

        if (otherCard != null) {
            otherCard.setOnClickListener(v -> {
                String count = otherCount != null ? otherCount.getText().toString() : "0 items";
                Toast.makeText(ProfileActivity.this, "Other: " + count, Toast.LENGTH_SHORT).show();
            });
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
        // Observe all products to get real-time counts
        productViewModel.getAllProducts().observe(this, new Observer<List<Product>>() {
            @Override
            public void onChanged(List<Product> products) {
                if (products != null) {
                    // Calculate total items
                    int totalItems = products.size();
                    if (totalItemsCount != null) {
                        totalItemsCount.setText(String.valueOf(totalItems));
                    }

                    // Calculate expired items
                    int expiredItems = 0;
                    // Calculate expiring soon items (within 7 days)
                    int expiringSoonItems = 0;

                    // Category counts
                    int dairyCount = 0;
                    int vegetablesCount = 0;
                    int fruitsCount = 0;
                    int meatsCount = 0;
                    int beveragesCount = 0;
                    int medicineCount = 0;
                    int otherCount = 0;

                    Date today = new Date();
                    long sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000L;

                    for (Product product : products) {
                        // Count by category
                        String category = product.getCategory();
                        if (category != null) {
                            switch (category) {
                                case "Dairy":
                                    dairyCount++;
                                    break;
                                case "Vegetables":
                                    vegetablesCount++;
                                    break;
                                case "Fruits":
                                    fruitsCount++;
                                    break;
                                case "Meats":
                                    meatsCount++;
                                    break;
                                case "Beverages":
                                    beveragesCount++;
                                    break;
                                case "Medicine":
                                    medicineCount++;
                                    break;
                                case "Other":
                                    otherCount++;
                                    break;
                            }
                        }

                        // Count expired items
                        Date expiryDate = product.getExpiryDate();
                        if (expiryDate != null && expiryDate.before(today)) {
                            expiredItems++;
                        }

                        // Count expiring soon items (within 7 days and not expired)
                        if (expiryDate != null && !expiryDate.before(today) &&
                                expiryDate.getTime() - today.getTime() <= sevenDaysInMillis) {
                            expiringSoonItems++;
                        }
                    }

                    // Update expired count
                    if (expiredCount != null) {
                        expiredCount.setText(String.valueOf(expiredItems));
                    }

                    // Update expiring soon count
                    if (expiringSoonCount != null) {
                        expiringSoonCount.setText(String.valueOf(expiringSoonItems));
                    }

                    // Update category counts - UPDATED with 7 categories
                    if (ProfileActivity.this.dairyCount != null) {
                        ProfileActivity.this.dairyCount.setText(dairyCount + " items");
                    }
                    if (ProfileActivity.this.vegetablesCount != null) {
                        ProfileActivity.this.vegetablesCount.setText(vegetablesCount + " items");
                    }
                    if (ProfileActivity.this.fruitsCount != null) {
                        ProfileActivity.this.fruitsCount.setText(fruitsCount + " items");
                    }
                    if (ProfileActivity.this.meatsCount != null) {
                        ProfileActivity.this.meatsCount.setText(meatsCount + " items");
                    }
                    if (ProfileActivity.this.beveragesCount != null) {
                        ProfileActivity.this.beveragesCount.setText(beveragesCount + " items");
                    }
                    if (ProfileActivity.this.medicineCount != null) {
                        ProfileActivity.this.medicineCount.setText(medicineCount + " items");
                    }
                    if (ProfileActivity.this.otherCount != null) {
                        ProfileActivity.this.otherCount.setText(otherCount + " items");
                    }

                    Log.d(TAG, "Profile data loaded - Total: " + totalItems +
                            ", Expired: " + expiredItems +
                            ", Expiring Soon: " + expiringSoonItems);
                }
            }
        });
    }
}
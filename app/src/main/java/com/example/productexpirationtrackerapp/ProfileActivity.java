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
import androidx.core.content.ContextCompat;
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
    private CardView safeCard;
    private CardView expiringSoonCard;
    private CardView expiredCard;

    // Category Cards
    private CardView dairyCard;
    private CardView vegetablesCard;
    private CardView fruitsCard;
    private CardView meatsCard;
    private CardView beveragesCard;
    private CardView medicineCard;
    private CardView otherCard;

    // Category Item Count TextViews
    private TextView dairyCount;
    private TextView vegetablesCount;
    private TextView fruitsCount;
    private TextView meatsCount;
    private TextView beveragesCount;
    private TextView medicineCount;
    private TextView otherCount;

    // Stat Count TextViews
    private TextView totalItemsCount;
    private TextView safeCount;
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

    // Main Layout - CHANGED: from LinearLayout to View to avoid ClassCastException
    private View mainLayout;
    private ScrollView scrollView;
    private SharedPreferences preferences;
    private ProductViewModel productViewModel;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize ViewModel
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        // Initialize UserRepository for theme
        userRepository = new UserRepository(getApplication());

        // Initialize preferences
        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        // Initialize all views
        initializeViews();

        // Setup bottom navigation
        setupBottomNavigation();

        // Setup click listeners
        setupClickListeners();

        // Apply theme
        applyThemeFromDatabase();

        // Load real data from database
        loadProfileData();

        Log.d(TAG, "ProfileActivity created successfully");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reapply theme when returning to this activity
        applyThemeFromDatabase();
        // Refresh data when returning to profile
        loadProfileData();
    }

    private void initializeViews() {
        try {
            // Main layout - CHANGED: using View type
            mainLayout = findViewById(R.id.mainLayout);

            // Header
            profileTitle = findViewById(R.id.profileTitle);
            profileSubtitle = findViewById(R.id.profileSubtitle);

            // Activity Stat Cards
            totalItemsCard = findViewById(R.id.total_items_card);
            safeCard = findViewById(R.id.safe_card);
            expiringSoonCard = findViewById(R.id.expiring_soon_card);
            expiredCard = findViewById(R.id.expired_card);

            // Stat Count TextViews
            totalItemsCount = findViewById(R.id.total_items_count);
            safeCount = findViewById(R.id.safe_count);
            expiringSoonCount = findViewById(R.id.expiring_soon_count);
            expiredCount = findViewById(R.id.expired_count);

            // Category Cards
            dairyCard = findViewById(R.id.dairy_card);
            vegetablesCard = findViewById(R.id.vegetables_card);
            fruitsCard = findViewById(R.id.fruits_card);
            meatsCard = findViewById(R.id.meats_card);
            beveragesCard = findViewById(R.id.beverages_card);
            medicineCard = findViewById(R.id.medicine_card);
            otherCard = findViewById(R.id.other_card);

            // Category Count TextViews
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

        if (safeCard != null) {
            safeCard.setOnClickListener(v -> {
                String count = safeCount != null ? safeCount.getText().toString() : "0";
                Toast.makeText(ProfileActivity.this, "Safe Items: " + count, Toast.LENGTH_SHORT).show();
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

        // Category cards
        if (dairyCard != null) {
            dairyCard.setOnClickListener(v -> {
                String count = dairyCount != null ? dairyCount.getText().toString() : "0 items";
                openCategoryDetail("Dairy", count);
            });
        }

        if (vegetablesCard != null) {
            vegetablesCard.setOnClickListener(v -> {
                String count = vegetablesCount != null ? vegetablesCount.getText().toString() : "0 items";
                openCategoryDetail("Vegetables", count);
            });
        }

        if (fruitsCard != null) {
            fruitsCard.setOnClickListener(v -> {
                String count = fruitsCount != null ? fruitsCount.getText().toString() : "0 items";
                openCategoryDetail("Fruits", count);
            });
        }

        if (meatsCard != null) {
            meatsCard.setOnClickListener(v -> {
                String count = meatsCount != null ? meatsCount.getText().toString() : "0 items";
                openCategoryDetail("Meats", count);
            });
        }

        if (beveragesCard != null) {
            beveragesCard.setOnClickListener(v -> {
                String count = beveragesCount != null ? beveragesCount.getText().toString() : "0 items";
                openCategoryDetail("Beverages", count);
            });
        }

        if (medicineCard != null) {
            medicineCard.setOnClickListener(v -> {
                String count = medicineCount != null ? medicineCount.getText().toString() : "0 items";
                openCategoryDetail("Medicine", count);
            });
        }

        if (otherCard != null) {
            otherCard.setOnClickListener(v -> {
                String count = otherCount != null ? otherCount.getText().toString() : "0 items";
                openCategoryDetail("Other", count);
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

    private void openCategoryDetail(String categoryName, String itemCount) {
        Log.d(TAG, "Opening category: " + categoryName);
        Intent intent = new Intent(ProfileActivity.this, CategoryDetailActivity.class);
        intent.putExtra("category_name", categoryName);
        intent.putExtra("category_count", itemCount);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    // ========== THEME METHODS ==========

    private void applyThemeFromDatabase() {
        User user = userRepository.getUserSync();

        if (user != null) {
            String theme = user.getColorTheme();
            Log.d(TAG, "Applying theme: " + theme);
            applyThemeColors(theme);
        } else {
            String theme = preferences.getString("color_theme", "light");
            Log.d(TAG, "No user, applying theme from prefs: " + theme);
            applyThemeColors(theme);
        }
    }

    private void applyThemeColors(String theme) {
        boolean isDarkTheme = theme.equals("dark") || theme.equals("black");

        int primaryColor;
        int backgroundColor;
        int textColor;
        int fabBackgroundColor;
        int fabIconColor;

        if (isDarkTheme) {
            // Dark theme colors
            primaryColor = ContextCompat.getColor(this, R.color.color_primary_dark);
            backgroundColor = ContextCompat.getColor(this, R.color.color_background_dark);
            textColor = ContextCompat.getColor(this, R.color.color_text_dark);
            fabBackgroundColor = ContextCompat.getColor(this, R.color.color_fab_dark);
            fabIconColor = ContextCompat.getColor(this, R.color.color_fab_icon_dark);
        } else {
            // Light theme colors
            primaryColor = ContextCompat.getColor(this, R.color.color_primary_light);
            backgroundColor = ContextCompat.getColor(this, R.color.color_background_light);
            textColor = ContextCompat.getColor(this, R.color.color_text_light);
            fabBackgroundColor = ContextCompat.getColor(this, R.color.color_fab_light);
            fabIconColor = ContextCompat.getColor(this, R.color.color_fab_icon_light);
        }

        Log.d(TAG, "Theme: " + (isDarkTheme ? "DARK" : "LIGHT"));

        // Apply to main layout background - FIXED: using View type
        if (mainLayout != null) {
            mainLayout.setBackgroundColor(backgroundColor);
        }

        // Apply to bottom navigation
        LinearLayout bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            bottomNavigation.setBackgroundColor(fabBackgroundColor);
        }

        // Apply to FAB
        if (addButton != null) {
            addButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(fabBackgroundColor));
            addButton.setImageTintList(android.content.res.ColorStateList.valueOf(fabIconColor));
        }

        // Set text colors
        if (profileTitle != null) profileTitle.setTextColor(textColor);
        if (profileSubtitle != null) profileSubtitle.setTextColor(textColor);

        // Bottom navigation icons and text colors
        int navIconColor = fabIconColor;

        if (navProfileIcon != null) navProfileIcon.setImageTintList(android.content.res.ColorStateList.valueOf(navIconColor));
        if (navProductsIcon != null) navProductsIcon.setImageTintList(android.content.res.ColorStateList.valueOf(navIconColor));
        if (navSettingsIcon != null) navSettingsIcon.setImageTintList(android.content.res.ColorStateList.valueOf(navIconColor));

        if (navProfileText != null) navProfileText.setTextColor(navIconColor);
        if (navProductsText != null) navProductsText.setTextColor(navIconColor);
        if (navSettingsText != null) navSettingsText.setTextColor(navIconColor);

        if (navProfileIndicator != null) navProfileIndicator.setBackgroundColor(navIconColor);
        if (navProductsIndicator != null) navProductsIndicator.setBackgroundColor(navIconColor);
        if (navSettingsIndicator != null) navSettingsIndicator.setBackgroundColor(navIconColor);

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
                    // Calculate safe items (more than 7 days away)
                    int safeItems = 0;

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

                        // Count expired, expiring soon, and safe items
                        Date expiryDate = product.getExpiryDate();
                        if (expiryDate != null) {
                            if (expiryDate.before(today)) {
                                expiredItems++;
                            } else if (expiryDate.getTime() - today.getTime() <= sevenDaysInMillis) {
                                expiringSoonItems++;
                            } else {
                                safeItems++;
                            }
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

                    // Update safe count
                    if (safeCount != null) {
                        safeCount.setText(String.valueOf(safeItems));
                    }

                    // Update category counts
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
                            ", Safe: " + safeItems +
                            ", Expiring Soon: " + expiringSoonItems +
                            ", Expired: " + expiredItems);
                }
            }
        });
    }
}
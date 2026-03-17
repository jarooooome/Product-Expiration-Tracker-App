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

import androidx.core.content.ContextCompat;
import android.widget.RelativeLayout;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    // Header Views
    private TextView profileTitle;
    private TextView profileSubtitle;

    // Activity Stat Cards
    private CardView totalItemsCard;
    private CardView safeCard; // NEW
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
    private TextView safeCount; // NEW
    private TextView expiringSoonCount;
    private TextView expiredCount;

    // Stat Labels (NEW)
    private TextView totalItemsLabel;
    private TextView safeLabel;
    private TextView expiringSoonLabel;
    private TextView expiredLabel;

    // Header and Section Views (NEW)
    private TextView yourActivityHeader;
    private TextView categoriesHeader;

    // Category Icons and Names (NEW)
    private TextView dairyIcon;
    private TextView dairyName;
    private TextView vegetablesIcon;
    private TextView vegetablesName;
    private TextView fruitsIcon;
    private TextView fruitsName;
    private TextView meatsIcon;
    private TextView meatsName;
    private TextView beveragesIcon;
    private TextView beveragesName;
    private TextView medicineIcon;
    private TextView medicineName;
    private TextView otherIcon;
    private TextView otherName;

    // Layout containers (NEW)
    private LinearLayout firstRowLayout;
    private LinearLayout secondRowLayout;
    private LinearLayout categoriesFirstRow;
    private LinearLayout categoriesSecondRow;
    private LinearLayout categoriesThirdRow;
    private LinearLayout categoriesFourthRow;

    // Floating Action Button
    private ImageView addButton;
    private ImageView infoButton;

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

    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply background color synchronously from SharedPrefs BEFORE setContentView to prevent blink
        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String quickTheme = preferences.getString("color_theme", "white");
        int quickBg = "black".equals(quickTheme)
                ? android.graphics.Color.parseColor("#121212")
                : android.graphics.Color.parseColor("#F5F5F5");
        getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(quickBg));

        setContentView(R.layout.activity_profile);

        // Initialize ViewModel
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        userRepository = new UserRepository(getApplication());

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
            safeCard = findViewById(R.id.safe_card); // NEW
            expiringSoonCard = findViewById(R.id.expiring_soon_card);
            expiredCard = findViewById(R.id.expired_card);

            // Stat Count TextViews
            totalItemsCount = findViewById(R.id.total_items_count);
            safeCount = findViewById(R.id.safe_count); // NEW
            expiringSoonCount = findViewById(R.id.expiring_soon_count);
            expiredCount = findViewById(R.id.expired_count);

            // Stat Labels (NEW)
            totalItemsLabel = findViewById(R.id.total_items_label);
            safeLabel = findViewById(R.id.safe_label);
            expiringSoonLabel = findViewById(R.id.expiring_soon_label);
            expiredLabel = findViewById(R.id.expired_label);

            // Header and Section Views (NEW)
            yourActivityHeader = findViewById(R.id.yourActivityHeader);
            categoriesHeader = findViewById(R.id.categoriesHeader);

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

            // Category Icons and Names (NEW)
            dairyIcon = findViewById(R.id.dairy_icon);
            dairyName = findViewById(R.id.dairy_name);
            vegetablesIcon = findViewById(R.id.vegetables_icon);
            vegetablesName = findViewById(R.id.vegetables_name);
            fruitsIcon = findViewById(R.id.fruits_icon);
            fruitsName = findViewById(R.id.fruits_name);
            meatsIcon = findViewById(R.id.meats_icon);
            meatsName = findViewById(R.id.meats_name);
            beveragesIcon = findViewById(R.id.beverages_icon);
            beveragesName = findViewById(R.id.beverages_name);
            medicineIcon = findViewById(R.id.medicine_icon);
            medicineName = findViewById(R.id.medicine_name);
            otherIcon = findViewById(R.id.other_icon);
            otherName = findViewById(R.id.other_name);

            // Layout containers (NEW)
            firstRowLayout = findViewById(R.id.firstRowLayout);
            secondRowLayout = findViewById(R.id.secondRowLayout);
            categoriesFirstRow = findViewById(R.id.categoriesFirstRow);
            categoriesSecondRow = findViewById(R.id.categoriesSecondRow);
            categoriesThirdRow = findViewById(R.id.categoriesThirdRow);
            categoriesFourthRow = findViewById(R.id.categoriesFourthRow);

            // Floating Action Button
            addButton = findViewById(R.id.addButton);
            infoButton = findViewById(R.id.infoButton);

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

    private void showInfoDialog() {
        // Build a custom dialog view
        android.widget.LinearLayout root = new android.widget.LinearLayout(this);
        root.setOrientation(android.widget.LinearLayout.VERTICAL);
        int pad = (int)(20 * getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);

        // Detect current theme for dialog colors
        String theme = preferences.getString("color_theme", "white");
        boolean isDark = "black".equals(theme);
        int dialogBg    = isDark ? android.graphics.Color.parseColor("#1E1E1E") : android.graphics.Color.WHITE;
        int textPrimary = isDark ? android.graphics.Color.WHITE  : android.graphics.Color.parseColor("#212121");
        int textSec     = isDark ? android.graphics.Color.LTGRAY : android.graphics.Color.parseColor("#757575");
        root.setBackgroundColor(dialogBg);

        // Helper to add a section title
        java.util.function.Consumer<String> addSection = title -> {
            android.widget.TextView tv = new android.widget.TextView(this);
            tv.setText(title);
            tv.setTextColor(textPrimary);
            tv.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 15);
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
            int topMargin = (int)(14 * getResources().getDisplayMetrics().density);
            android.widget.LinearLayout.LayoutParams lp =
                    new android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.topMargin = topMargin;
            tv.setLayoutParams(lp);
            root.addView(tv);
        };

        // Helper to add a colour-dot row
        java.util.function.BiConsumer<Integer, String> addColorRow = (color, desc) -> {
            android.widget.LinearLayout row = new android.widget.LinearLayout(this);
            row.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            int rowPad = (int)(6 * getResources().getDisplayMetrics().density);
            row.setPadding(0, rowPad, 0, rowPad);

            // Colour dot
            android.view.View dot = new android.view.View(this);
            int dotSize = (int)(16 * getResources().getDisplayMetrics().density);
            android.widget.LinearLayout.LayoutParams dotLp =
                    new android.widget.LinearLayout.LayoutParams(dotSize, dotSize);
            dotLp.setMarginEnd((int)(12 * getResources().getDisplayMetrics().density));
            dot.setLayoutParams(dotLp);
            android.graphics.drawable.GradientDrawable circle = new android.graphics.drawable.GradientDrawable();
            circle.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            circle.setColor(color);
            dot.setBackground(circle);
            row.addView(dot);

            android.widget.TextView tv = new android.widget.TextView(this);
            tv.setText(desc);
            tv.setTextColor(textSec);
            tv.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 13);
            row.addView(tv);
            root.addView(row);
        };

        // Helper for plain text rows
        java.util.function.Consumer<String> addNote = note -> {
            android.widget.TextView tv = new android.widget.TextView(this);
            tv.setText("• " + note);
            tv.setTextColor(textSec);
            tv.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 13);
            int topM = (int)(4 * getResources().getDisplayMetrics().density);
            android.widget.LinearLayout.LayoutParams lp =
                    new android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.topMargin = topM;
            tv.setLayoutParams(lp);
            root.addView(tv);
        };

        // ── Colour Coding ─────────────────────────────────────────────────
        addSection.accept("Colour Coding");
        addColorRow.accept(android.graphics.Color.TRANSPARENT, "No badge — 30 or more days remaining");
        addColorRow.accept(android.graphics.Color.parseColor("#FFC107"), "Yellow — 8 to 30 days remaining");
        addColorRow.accept(android.graphics.Color.parseColor("#FF6D00"), "Orange — 1 to 7 days remaining");
        addColorRow.accept(android.graphics.Color.parseColor("#D50000"), "Red — expired (past expiry date)");

        // ── Categories ────────────────────────────────────────────────────
        addSection.accept("Categories");
        addNote.accept("Dairy, Vegetables, Fruits, Meats, Beverages, Medicine, Other");
        addNote.accept("Tap a category card on this screen to open its full product list");
        addNote.accept("Use the category chips on the Products screen to filter by type");

        // ── How to use ────────────────────────────────────────────────────
        addSection.accept("💡  Quick Tips");
        addNote.accept("Tap + to add a new product with its expiry date");
        addNote.accept("Use the barcode scanner (in Products) to auto-fill product details");
        addNote.accept("Use the search bar to find specific products by name");
        addNote.accept("Tap the history icon in Products to view your consumed product history");
        addNote.accept("Products are sorted by expiry date — soonest expiring first");
        addNote.accept("Long-press a product card to enter bulk selection mode");

        // ── Settings ──────────────────────────────────────────────────────
        addSection.accept("⚙️  Settings");
        addNote.accept("Set your nickname (up to 8 letters) in Settings → User Profile");
        addNote.accept("Adjust how many days before expiry you get notified via the reminder slider");
        addNote.accept("Choose Light or Dark theme under App Theme");
        addNote.accept("Set a daily notification time for expiry alerts");

        // ── Expiry Logic ──────────────────────────────────────────────────
        addSection.accept("Expiry Rules");
        addNote.accept("'Expiring Soon' triggers based on your reminder frequency in Settings (default: 3 days)");
        addNote.accept("'Expired' shows products whose expiry date has already passed");
        addNote.accept("Check the Products page regularly to avoid waste");

        // Build & show dialog
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("App Guide & Information");
        builder.setView(new android.widget.ScrollView(this) {{
            addView(root);
        }});
        builder.setPositiveButton("Got it!", null);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

        // Style dialog background to match theme
        if (dialog.getWindow() != null) {
            android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
            bg.setColor(dialogBg);
            bg.setCornerRadius(24f);
            dialog.getWindow().setBackgroundDrawable(bg);
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
        // Don't set colors here - they're already set by applyThemeColors()
        if (indicator != null) {
            indicator.setVisibility(View.VISIBLE);
        }
    }

    private void setInactiveNavItem(LinearLayout navItem, ImageView icon, TextView text, View indicator) {
        // Don't set colors here - they're already set by applyThemeColors()
        if (indicator != null) {
            indicator.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        // Info button — shows color coding and app guide
        if (infoButton != null) {
            infoButton.setOnClickListener(v -> showInfoDialog());
        }

        // Activity stat cards
        if (totalItemsCard != null) {
            totalItemsCard.setOnClickListener(v -> {
                String count = totalItemsCount != null ? totalItemsCount.getText().toString() : "0";
                Toast.makeText(ProfileActivity.this, "Total Items: " + count, Toast.LENGTH_SHORT).show();
            });
        }

        if (safeCard != null) { // NEW
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

        // Category cards - OPEN CategoryDetailActivity with filter
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
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        if (navSettings != null) {
            navSettings.setOnClickListener(v -> {
                Log.d(TAG, "Settings navigation clicked");
                Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
    }

    // NEW - Open category detail activity
    private void openCategoryDetail(String categoryName, String itemCount) {
        Log.d(TAG, "Opening category: " + categoryName);
        Intent intent = new Intent(ProfileActivity.this, CategoryDetailActivity.class);
        intent.putExtra("category_name", categoryName);
        intent.putExtra("category_count", itemCount);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void applyTheme() {
        Log.d(TAG, "applyTheme() called");

        // Get theme from database asynchronously
        userRepository.getUser(new UserRepository.UserRepositoryCallback() {
            @Override
            public void onUserLoaded(User user) {
                String theme = "white"; // Default
                String source = "default";

                if (user != null) {
                    Log.d(TAG, "User found in database");
                    String dbTheme = user.getColorTheme();
                    Log.d(TAG, "Database theme value: '" + dbTheme + "'");

                    if (dbTheme != null && !dbTheme.isEmpty()) {
                        theme = dbTheme;
                        source = "database";
                        Log.d(TAG, "Using theme from DATABASE: " + theme);
                    } else {
                        // Fallback to SharedPreferences
                        source = "sharedpreferences (fallback - db theme empty)";
                        theme = preferences.getString("color_theme", "white");
                        Log.d(TAG, "Database theme empty, using SharedPreferences: " + theme);
                        Log.d(TAG, "SharedPreferences color_theme value: " + preferences.getString("color_theme", "NOT_FOUND"));
                    }
                } else {
                    Log.d(TAG, "No user found in database");
                    // Fallback to SharedPreferences
                    source = "sharedpreferences (fallback - no user)";
                    theme = preferences.getString("color_theme", "white");
                    Log.d(TAG, "SharedPreferences color_theme value: " + preferences.getString("color_theme", "NOT_FOUND"));
                }

                final String finalTheme = theme;
                final String finalSource = source;

                runOnUiThread(() -> {
                    Log.d(TAG, "Applying theme from " + finalSource + " with value: " + finalTheme);
                    applyThemeColors(finalTheme);
                });
            }
        });
    }

    private void applyThemeColors(String theme) {
        Log.d(TAG, "applyThemeColors() called with theme: " + theme);

        // Get colors based on theme
        int fabBackgroundColor;
        int bottomNavColor;
        int textColor;
        int iconColor;
        int indicatorColor;
        int backgroundColor;
        int primaryTextColor;
        int secondaryTextColor;
        int cardBackgroundColor;
        int categoryDairyColor;
        int categoryVegetablesColor;
        int categoryFruitsColor;
        int categoryMeatsColor;
        int categoryBeveragesColor;
        int categoryMedicineColor;
        int categoryOtherColor;

        if ("black".equals(theme)) {
            // Black theme
            fabBackgroundColor = Color.parseColor("#1E1E1E");
            bottomNavColor = ContextCompat.getColor(this, R.color.color_nav_background_black);
            textColor = Color.WHITE;
            iconColor = Color.WHITE;
            indicatorColor = Color.parseColor("#4CAF50");
            backgroundColor = ContextCompat.getColor(this, R.color.color_background_black);
            primaryTextColor = Color.WHITE;
            secondaryTextColor = Color.LTGRAY;
            cardBackgroundColor = ContextCompat.getColor(this, R.color.color_surface_black);
            categoryDairyColor = ContextCompat.getColor(this, R.color.category_dairy_dark);
            categoryVegetablesColor = ContextCompat.getColor(this, R.color.category_vegetables_dark);
            categoryFruitsColor = ContextCompat.getColor(this, R.color.category_fruits_dark);
            categoryMeatsColor = ContextCompat.getColor(this, R.color.category_meats_dark);
            categoryBeveragesColor = ContextCompat.getColor(this, R.color.category_beverages_dark);
            categoryMedicineColor = ContextCompat.getColor(this, R.color.category_medicine_dark);
            categoryOtherColor = ContextCompat.getColor(this, R.color.category_other_dark);
        } else {
            // White theme (default)
            fabBackgroundColor = Color.parseColor("#4CAF50");
            bottomNavColor = Color.WHITE;
            textColor = Color.BLACK;
            iconColor = Color.BLACK;
            indicatorColor = Color.parseColor("#388E3C");
            backgroundColor = ContextCompat.getColor(this, R.color.color_background_white);
            primaryTextColor = Color.BLACK;
            secondaryTextColor = Color.DKGRAY;
            cardBackgroundColor = ContextCompat.getColor(this, R.color.color_surface_white);
            categoryDairyColor = ContextCompat.getColor(this, R.color.category_dairy_light);
            categoryVegetablesColor = ContextCompat.getColor(this, R.color.category_vegetables_light);
            categoryFruitsColor = ContextCompat.getColor(this, R.color.category_fruits_light);
            categoryMeatsColor = ContextCompat.getColor(this, R.color.category_meats_light);
            categoryBeveragesColor = ContextCompat.getColor(this, R.color.category_beverages_light);
            categoryMedicineColor = ContextCompat.getColor(this, R.color.category_medicine_light);
            categoryOtherColor = ContextCompat.getColor(this, R.color.category_other_light);
        }

        // Apply to main background
        RelativeLayout mainLayout = findViewById(R.id.mainLayout);
        if (mainLayout != null) {
            mainLayout.setBackgroundColor(backgroundColor);
        }

        // Apply to bottom navigation
        LinearLayout bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            android.graphics.drawable.GradientDrawable navRounded = new android.graphics.drawable.GradientDrawable();
            navRounded.setColor(bottomNavColor);
            float[] navRadii = {48f, 48f, 48f, 48f, 0f, 0f, 0f, 0f};
            navRounded.setCornerRadii(navRadii);
            bottomNavigation.setBackground(navRounded);
        }

        // Apply to FAB
        if (addButton != null) {
            addButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(fabBackgroundColor));
            addButton.setImageTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
        }

        // Info button tint — matches primary text color for visibility
        if (infoButton != null) {
            infoButton.setImageTintList(android.content.res.ColorStateList.valueOf(primaryTextColor));
        }

        // Bottom navigation icons and text colors
        if (navProfileIcon != null) navProfileIcon.setImageTintList(android.content.res.ColorStateList.valueOf(iconColor));
        if (navProductsIcon != null) navProductsIcon.setImageTintList(android.content.res.ColorStateList.valueOf(iconColor));
        if (navSettingsIcon != null) navSettingsIcon.setImageTintList(android.content.res.ColorStateList.valueOf(iconColor));

        if (navProfileText != null) navProfileText.setTextColor(textColor);
        if (navProductsText != null) navProductsText.setTextColor(textColor);
        if (navSettingsText != null) navSettingsText.setTextColor(textColor);

        if (navProfileIndicator != null) navProfileIndicator.setBackgroundColor(indicatorColor);
        if (navProductsIndicator != null) navProductsIndicator.setBackgroundColor(indicatorColor);
        if (navSettingsIndicator != null) navSettingsIndicator.setBackgroundColor(indicatorColor);

        // Apply text colors to main content
        if (profileTitle != null) profileTitle.setTextColor(primaryTextColor);
        if (profileSubtitle != null) profileSubtitle.setTextColor(secondaryTextColor);

        // Apply to section headers
        if (yourActivityHeader != null) yourActivityHeader.setTextColor(primaryTextColor);
        if (categoriesHeader != null) categoriesHeader.setTextColor(primaryTextColor);

        // Apply to stat numbers
        if (totalItemsCount != null) totalItemsCount.setTextColor(primaryTextColor);
        if (safeCount != null) safeCount.setTextColor(primaryTextColor); // transparent badge = normal text
        if (expiringSoonCount != null) expiringSoonCount.setTextColor(ContextCompat.getColor(this, R.color.color_soon));
        if (expiredCount != null) expiredCount.setTextColor(ContextCompat.getColor(this, R.color.color_expired));

        // Apply to stat labels
        if (totalItemsLabel != null) totalItemsLabel.setTextColor(secondaryTextColor);
        if (safeLabel != null) safeLabel.setTextColor(secondaryTextColor);
        if (expiringSoonLabel != null) expiringSoonLabel.setTextColor(secondaryTextColor);
        if (expiredLabel != null) expiredLabel.setTextColor(secondaryTextColor);

        // FIXED: ALL categories now use the same text color as "Other"
        // This ensures consistent text color across all category cards
        int categoryNameColor = primaryTextColor;  // Use same as primary text (white in dark, black in light)
        int categoryCountColor = secondaryTextColor; // Use same as secondary text

        // Apply to category counts
        if (dairyCount != null) dairyCount.setTextColor(categoryCountColor);
        if (vegetablesCount != null) vegetablesCount.setTextColor(categoryCountColor);
        if (fruitsCount != null) fruitsCount.setTextColor(categoryCountColor);
        if (meatsCount != null) meatsCount.setTextColor(categoryCountColor);
        if (beveragesCount != null) beveragesCount.setTextColor(categoryCountColor);
        if (medicineCount != null) medicineCount.setTextColor(categoryCountColor);
        if (otherCount != null) otherCount.setTextColor(categoryCountColor);

        // Apply to category names
        if (dairyName != null) dairyName.setTextColor(categoryNameColor);
        if (vegetablesName != null) vegetablesName.setTextColor(categoryNameColor);
        if (fruitsName != null) fruitsName.setTextColor(categoryNameColor);
        if (meatsName != null) meatsName.setTextColor(categoryNameColor);
        if (beveragesName != null) beveragesName.setTextColor(categoryNameColor);
        if (medicineName != null) medicineName.setTextColor(categoryNameColor);
        if (otherName != null) otherName.setTextColor(categoryNameColor);

        // Apply to category icons (emojis)
        if (dairyIcon != null) dairyIcon.setTextColor(categoryNameColor);
        if (vegetablesIcon != null) vegetablesIcon.setTextColor(categoryNameColor);
        if (fruitsIcon != null) fruitsIcon.setTextColor(categoryNameColor);
        if (meatsIcon != null) meatsIcon.setTextColor(categoryNameColor);
        if (beveragesIcon != null) beveragesIcon.setTextColor(categoryNameColor);
        if (medicineIcon != null) medicineIcon.setTextColor(categoryNameColor);
        if (otherIcon != null) otherIcon.setTextColor(categoryNameColor);

        // Apply to layout backgrounds
        if (firstRowLayout != null) firstRowLayout.setBackgroundColor(backgroundColor);
        if (secondRowLayout != null) secondRowLayout.setBackgroundColor(backgroundColor);
        if (categoriesFirstRow != null) categoriesFirstRow.setBackgroundColor(backgroundColor);
        if (categoriesSecondRow != null) categoriesSecondRow.setBackgroundColor(backgroundColor);
        if (categoriesThirdRow != null) categoriesThirdRow.setBackgroundColor(backgroundColor);
        if (categoriesFourthRow != null) categoriesFourthRow.setBackgroundColor(backgroundColor);

        // Apply to card backgrounds
        if (totalItemsCard != null) totalItemsCard.setCardBackgroundColor(cardBackgroundColor);
        if (safeCard != null) safeCard.setCardBackgroundColor(cardBackgroundColor);
        if (expiringSoonCard != null) expiringSoonCard.setCardBackgroundColor(cardBackgroundColor);
        if (expiredCard != null) expiredCard.setCardBackgroundColor(cardBackgroundColor);

        // Apply to category card backgrounds
        if (dairyCard != null) dairyCard.setCardBackgroundColor(categoryDairyColor);
        if (vegetablesCard != null) vegetablesCard.setCardBackgroundColor(categoryVegetablesColor);
        if (fruitsCard != null) fruitsCard.setCardBackgroundColor(categoryFruitsColor);
        if (meatsCard != null) meatsCard.setCardBackgroundColor(categoryMeatsColor);
        if (beveragesCard != null) beveragesCard.setCardBackgroundColor(categoryBeveragesColor);
        if (medicineCard != null) medicineCard.setCardBackgroundColor(categoryMedicineColor);
        if (otherCard != null) otherCard.setCardBackgroundColor(categoryOtherColor);

        Log.d(TAG, "Theme applied to profile activity: " + theme);
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
                    long oneDayInMillis    = 24 * 60 * 60 * 1000L;
                    long sevenDaysInMillis = 7  * oneDayInMillis;
                    long thirtyDaysInMillis= 30 * oneDayInMillis;

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
                            long diff = expiryDate.getTime() - today.getTime();
                            if (expiryDate.before(today)) {
                                expiredItems++;
                            } else if (diff <= sevenDaysInMillis) {
                                expiringSoonItems++; // orange: 1-7 days
                            } else {
                                safeItems++; // yellow (8-30 days) + transparent (30+)
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

                    // Update safe count (NEW)
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
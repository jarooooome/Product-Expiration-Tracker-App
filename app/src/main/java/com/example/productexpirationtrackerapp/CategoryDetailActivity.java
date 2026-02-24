package com.example.productexpirationtrackerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CategoryDetailActivity extends AppCompatActivity {

    private static final String TAG = "CategoryDetail";

    // UI Components
    private TextView categoryTitle;
    private TextView categoryItemCount;
    private TextView expiringSoonCount;
    private TextView expiredCount;
    private TextView safeCount;
    private TextView backButton;
    private LinearLayout mainLayout;
    private LinearLayout headerLayout;

    // Filter Buttons
    private Button filterAll;
    private Button filterSafe;
    private Button filterSoon;
    private Button filterExpired;
    private View filterAllIndicator;
    private View filterSafeIndicator;
    private View filterSoonIndicator;
    private View filterExpiredIndicator;

    // View Toggle Icons
    private ImageView listViewButton;
    private ImageView gridViewButton;
    private boolean isListView = true; // Default to list view

    // RecyclerView
    private RecyclerView productsRecyclerView;
    private ProductAdapter adapter;
    private ArrayList<Product> allCategoryProducts;
    private ArrayList<Product> filteredProducts;

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

    // Data
    private String categoryName;
    private String categoryCount;
    private ProductViewModel productViewModel;
    private UserRepository userRepository;

    // Current filter
    private String currentFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_detail);

        // Get intent data
        categoryName = getIntent().getStringExtra("category_name");
        categoryCount = getIntent().getStringExtra("category_count");

        // Initialize ViewModel
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        // Initialize UserRepository for theme
        userRepository = new UserRepository(getApplication());

        // Initialize views
        initializeViews();

        // Setup filter buttons
        setupFilterButtons();

        // Setup view toggle
        setupViewToggle();

        // Setup bottom navigation
        setupBottomNavigation();

        // Setup RecyclerView
        setupRecyclerView();

        // Apply theme
        applyThemeFromDatabase();

        // Load category products
        loadCategoryProducts();

        Log.d(TAG, "CategoryDetailActivity created for: " + categoryName);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reapply theme when returning
        applyThemeFromDatabase();
    }

    private void initializeViews() {
        mainLayout = findViewById(R.id.mainLayout);
        headerLayout = findViewById(R.id.headerLayout);
        backButton = findViewById(R.id.backButton);

        // Header
        categoryTitle = findViewById(R.id.categoryTitle);
        categoryItemCount = findViewById(R.id.categoryItemCount);
        expiringSoonCount = findViewById(R.id.expiringSoonCount);
        expiredCount = findViewById(R.id.expiredCount);
        safeCount = findViewById(R.id.safeCount);

        // Set category name
        if (categoryTitle != null) {
            categoryTitle.setText(categoryName);
        }
        if (categoryItemCount != null) {
            categoryItemCount.setText(categoryCount);
        }

        // Back button click listener
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Filter Buttons
        filterAll = findViewById(R.id.filterAll);
        filterSafe = findViewById(R.id.filterSafe);
        filterSoon = findViewById(R.id.filterSoon);
        filterExpired = findViewById(R.id.filterExpired);
        filterAllIndicator = findViewById(R.id.filterAllIndicator);
        filterSafeIndicator = findViewById(R.id.filterSafeIndicator);
        filterSoonIndicator = findViewById(R.id.filterSoonIndicator);
        filterExpiredIndicator = findViewById(R.id.filterExpiredIndicator);

        // View Toggle Icons
        listViewButton = findViewById(R.id.listViewButton);
        gridViewButton = findViewById(R.id.gridViewButton);

        // RecyclerView
        productsRecyclerView = findViewById(R.id.productsRecyclerView);

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
    }

    // ========== THEME METHODS ==========

    private void applyThemeFromDatabase() {
        User user = userRepository.getUserSync();

        if (user != null) {
            String theme = user.getColorTheme();
            applyThemeColors(theme);
        } else {
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            String theme = prefs.getString("color_theme", "light");
            applyThemeColors(theme);
        }
    }

    private void applyThemeColors(String theme) {
        boolean isDarkTheme = theme.equals("dark") || theme.equals("black");

        int primaryColor;
        int backgroundColor;
        int textColor;
        int accentColor;

        if (isDarkTheme) {
            // Dark theme colors
            primaryColor = ContextCompat.getColor(this, R.color.color_primary_dark);
            backgroundColor = ContextCompat.getColor(this, R.color.color_background_dark);
            textColor = ContextCompat.getColor(this, R.color.color_text_dark);
            accentColor = Color.parseColor("#64B5F6"); // Light blue for active filter
        } else {
            // Light theme colors
            primaryColor = ContextCompat.getColor(this, R.color.color_primary_light);
            backgroundColor = ContextCompat.getColor(this, R.color.color_background_light);
            textColor = ContextCompat.getColor(this, R.color.color_text_light);
            accentColor = Color.parseColor("#4361EE"); // Original blue for active filter
        }

        // Apply background to main layout
        if (mainLayout != null) {
            mainLayout.setBackgroundColor(backgroundColor);
        }

        // Apply header background
        if (headerLayout != null) {
            headerLayout.setBackgroundColor(primaryColor);
        }

        // Set text colors for header
        if (categoryTitle != null) categoryTitle.setTextColor(textColor);
        if (categoryItemCount != null) categoryItemCount.setTextColor(textColor);
        if (backButton != null) backButton.setTextColor(textColor);

        // Set count text colors
        if (safeCount != null) safeCount.setTextColor(textColor);
        if (expiringSoonCount != null) expiringSoonCount.setTextColor(textColor);
        if (expiredCount != null) expiredCount.setTextColor(textColor);

        // Update filter button colors
        updateFilterButtonColors(isDarkTheme, accentColor);

        // Update view toggle icon colors
        updateViewToggleColors(isDarkTheme, accentColor);

        // Apply to bottom navigation
        LinearLayout bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            bottomNavigation.setBackgroundColor(primaryColor);
        }

        // Bottom navigation icons and text colors
        int navIconColor = Color.WHITE; // Bottom nav icons are always white

        if (navProfileIcon != null) navProfileIcon.setImageTintList(android.content.res.ColorStateList.valueOf(navIconColor));
        if (navProductsIcon != null) navProductsIcon.setImageTintList(android.content.res.ColorStateList.valueOf(navIconColor));
        if (navSettingsIcon != null) navSettingsIcon.setImageTintList(android.content.res.ColorStateList.valueOf(navIconColor));

        if (navProfileText != null) navProfileText.setTextColor(navIconColor);
        if (navProductsText != null) navProductsText.setTextColor(navIconColor);
        if (navSettingsText != null) navSettingsText.setTextColor(navIconColor);

        if (navProfileIndicator != null) navProfileIndicator.setBackgroundColor(navIconColor);
        if (navProductsIndicator != null) navProductsIndicator.setBackgroundColor(navIconColor);
        if (navSettingsIndicator != null) navSettingsIndicator.setBackgroundColor(navIconColor);

        // Reapply active filter to update its color
        setActiveFilter(currentFilter);
    }

    private void updateFilterButtonColors(boolean isDarkTheme, int accentColor) {
        // Reset filter button colors
        resetFilterButton(filterAll, filterAllIndicator, isDarkTheme);
        resetFilterButton(filterSafe, filterSafeIndicator, isDarkTheme);
        resetFilterButton(filterSoon, filterSoonIndicator, isDarkTheme);
        resetFilterButton(filterExpired, filterExpiredIndicator, isDarkTheme);

        // Reapply active filter
        setActiveFilter(currentFilter);
    }

    private void resetFilterButton(Button button, View indicator, boolean isDarkTheme) {
        if (button != null) {
            if (isDarkTheme) {
                button.setTextColor(Color.LTGRAY);
            } else {
                button.setTextColor(Color.parseColor("#999999"));
            }
            button.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
        if (indicator != null) {
            indicator.setVisibility(View.GONE);
        }
    }

    private void updateViewToggleColors(boolean isDarkTheme, int accentColor) {
        if (listViewButton != null) {
            if (isListView) {
                listViewButton.setImageTintList(android.content.res.ColorStateList.valueOf(accentColor));
                if (gridViewButton != null) {
                    gridViewButton.setImageTintList(android.content.res.ColorStateList.valueOf(isDarkTheme ? Color.LTGRAY : Color.parseColor("#999999")));
                }
            } else {
                if (gridViewButton != null) {
                    gridViewButton.setImageTintList(android.content.res.ColorStateList.valueOf(accentColor));
                }
                listViewButton.setImageTintList(android.content.res.ColorStateList.valueOf(isDarkTheme ? Color.LTGRAY : Color.parseColor("#999999")));
            }
        }
    }

    private void setupFilterButtons() {
        // Set All as active by default
        setActiveFilter("All");

        filterAll.setOnClickListener(v -> {
            currentFilter = "All";
            setActiveFilter("All");
            applyFilter();
        });

        filterSafe.setOnClickListener(v -> {
            currentFilter = "Safe";
            setActiveFilter("Safe");
            applyFilter();
        });

        filterSoon.setOnClickListener(v -> {
            currentFilter = "Soon";
            setActiveFilter("Soon");
            applyFilter();
        });

        filterExpired.setOnClickListener(v -> {
            currentFilter = "Expired";
            setActiveFilter("Expired");
            applyFilter();
        });
    }

    private void setActiveFilter(String filter) {
        // Get current theme
        boolean isDarkTheme = false;
        User user = userRepository.getUserSync();
        if (user != null) {
            isDarkTheme = user.getColorTheme().equals("dark") || user.getColorTheme().equals("black");
        }

        int activeColor;
        if (isDarkTheme) {
            activeColor = Color.parseColor("#64B5F6"); // Light blue for dark theme
        } else {
            activeColor = Color.parseColor("#4361EE"); // Original blue for light theme
        }

        // Reset all filters
        resetFilterButton(filterAll, filterAllIndicator, isDarkTheme);
        resetFilterButton(filterSafe, filterSafeIndicator, isDarkTheme);
        resetFilterButton(filterSoon, filterSoonIndicator, isDarkTheme);
        resetFilterButton(filterExpired, filterExpiredIndicator, isDarkTheme);

        // Set active filter based on filter name
        Button activeButton = null;
        View activeIndicator = null;

        switch (filter) {
            case "All":
                activeButton = filterAll;
                activeIndicator = filterAllIndicator;
                break;
            case "Safe":
                activeButton = filterSafe;
                activeIndicator = filterSafeIndicator;
                break;
            case "Soon":
                activeButton = filterSoon;
                activeIndicator = filterSoonIndicator;
                break;
            case "Expired":
                activeButton = filterExpired;
                activeIndicator = filterExpiredIndicator;
                break;
        }

        if (activeButton != null) {
            activeButton.setTextColor(activeColor);
            activeButton.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        if (activeIndicator != null) {
            activeIndicator.setVisibility(View.VISIBLE);
        }
    }

    // Setup view toggle between list and grid
    private void setupViewToggle() {
        if (listViewButton != null) {
            listViewButton.setOnClickListener(v -> {
                if (!isListView) {
                    isListView = true;
                    // Get current theme for icon color
                    boolean isDarkTheme = false;
                    User user = userRepository.getUserSync();
                    if (user != null) {
                        isDarkTheme = user.getColorTheme().equals("dark") || user.getColorTheme().equals("black");
                    }
                    int accentColor = isDarkTheme ? Color.parseColor("#64B5F6") : Color.parseColor("#4361EE");

                    // Update icon colors
                    listViewButton.setImageTintList(android.content.res.ColorStateList.valueOf(accentColor));
                    gridViewButton.setImageTintList(android.content.res.ColorStateList.valueOf(isDarkTheme ? Color.LTGRAY : Color.parseColor("#999999")));

                    // Change layout manager to LinearLayout (list)
                    LinearLayoutManager layoutManager = new LinearLayoutManager(CategoryDetailActivity.this);
                    productsRecyclerView.setLayoutManager(layoutManager);
                    adapter.notifyDataSetChanged();
                }
            });
        }

        if (gridViewButton != null) {
            gridViewButton.setOnClickListener(v -> {
                if (isListView) {
                    isListView = false;
                    // Get current theme for icon color
                    boolean isDarkTheme = false;
                    User user = userRepository.getUserSync();
                    if (user != null) {
                        isDarkTheme = user.getColorTheme().equals("dark") || user.getColorTheme().equals("black");
                    }
                    int accentColor = isDarkTheme ? Color.parseColor("#64B5F6") : Color.parseColor("#4361EE");

                    // Update icon colors
                    gridViewButton.setImageTintList(android.content.res.ColorStateList.valueOf(accentColor));
                    listViewButton.setImageTintList(android.content.res.ColorStateList.valueOf(isDarkTheme ? Color.LTGRAY : Color.parseColor("#999999")));

                    // Change layout manager to GridLayout (2 columns)
                    GridLayoutManager gridLayoutManager = new GridLayoutManager(CategoryDetailActivity.this, 2);
                    productsRecyclerView.setLayoutManager(gridLayoutManager);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void setupBottomNavigation() {
        // Set all as inactive (we're in a detail page)
        setInactiveNavItem(navProfile, navProfileIcon, navProfileText, navProfileIndicator);
        setInactiveNavItem(navProducts, navProductsIcon, navProductsText, navProductsIndicator);
        setInactiveNavItem(navSettings, navSettingsIcon, navSettingsText, navSettingsIndicator);

        // Profile click listener
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Log.d(TAG, "Profile navigation clicked");
                Intent intent = new Intent(CategoryDetailActivity.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            });
        }

        // Products click listener
        if (navProducts != null) {
            navProducts.setOnClickListener(v -> {
                Log.d(TAG, "Products navigation clicked");
                Intent intent = new Intent(CategoryDetailActivity.this, ProductListActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            });
        }

        // Settings click listener
        if (navSettings != null) {
            navSettings.setOnClickListener(v -> {
                Log.d(TAG, "Settings navigation clicked");
                Intent intent = new Intent(CategoryDetailActivity.this, SettingsActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            });
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

    private void setupRecyclerView() {
        // Initialize lists FIRST
        filteredProducts = new ArrayList<>();
        allCategoryProducts = new ArrayList<>();

        // THEN create adapter with initialized list
        adapter = new ProductAdapter(filteredProducts);

        adapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Product product = filteredProducts.get(position);
                Log.d(TAG, "Opening product details: " + product.getName());

                Intent intent = new Intent(CategoryDetailActivity.this, ProductDetailActivity.class);
                intent.putExtra("product_id", product.getId());
                intent.putExtra("product_name", product.getName());

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                intent.putExtra("expiry_date", sdf.format(product.getExpiryDate()));

                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }

            @Override
            public void onItemLongClick(int position) {
                Product product = filteredProducts.get(position);
                productViewModel.delete(product);

                Toast.makeText(CategoryDetailActivity.this,
                        "Removed: " + product.getName(),
                        Toast.LENGTH_SHORT).show();

                Log.d(TAG, "Item long clicked and removed: " + product.getName());
            }
        });

        if (productsRecyclerView != null) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            productsRecyclerView.setLayoutManager(layoutManager);
            productsRecyclerView.setAdapter(adapter);
        }
    }

    private void loadCategoryProducts() {
        productViewModel.getProductsByCategory(categoryName).observe(this, new Observer<List<Product>>() {
            @Override
            public void onChanged(List<Product> products) {
                if (products != null) {
                    allCategoryProducts.clear();
                    allCategoryProducts.addAll(products);
                    updateCounts(products);
                    applyFilter();
                }
            }
        });
    }

    private void updateCounts(List<Product> products) {
        Date today = new Date();
        long sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000L;

        int expired = 0;
        int soon = 0;
        int safe = 0;

        for (Product product : products) {
            Date expiryDate = product.getExpiryDate();
            if (expiryDate != null) {
                if (expiryDate.before(today)) {
                    expired++;
                } else if (expiryDate.getTime() - today.getTime() <= sevenDaysInMillis) {
                    soon++;
                } else {
                    safe++;
                }
            }
        }

        if (expiredCount != null) expiredCount.setText(String.valueOf(expired));
        if (expiringSoonCount != null) expiringSoonCount.setText(String.valueOf(soon));
        if (safeCount != null) safeCount.setText(String.valueOf(safe));
    }

    private void applyFilter() {
        filteredProducts.clear();

        Date today = new Date();
        long sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000L;

        for (Product product : allCategoryProducts) {
            Date expiryDate = product.getExpiryDate();

            switch (currentFilter) {
                case "All":
                    filteredProducts.add(product);
                    break;
                case "Safe":
                    if (expiryDate != null && expiryDate.getTime() - today.getTime() > sevenDaysInMillis) {
                        filteredProducts.add(product);
                    }
                    break;
                case "Soon":
                    if (expiryDate != null && !expiryDate.before(today) &&
                            expiryDate.getTime() - today.getTime() <= sevenDaysInMillis) {
                        filteredProducts.add(product);
                    }
                    break;
                case "Expired":
                    if (expiryDate != null && expiryDate.before(today)) {
                        filteredProducts.add(product);
                    }
                    break;
            }
        }

        adapter.updateData(filteredProducts);
        Log.d(TAG, "Filter '" + currentFilter + "' applied - showing " + filteredProducts.size() + " products");
    }
}
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

    // Filter Buttons - UPDATED: changed filterUrgent to filterExpired
    private Button filterAll;
    private Button filterSafe;
    private Button filterSoon;
    private Button filterExpired;  // CHANGED from filterUrgent
    private View filterAllIndicator;
    private View filterSafeIndicator;
    private View filterSoonIndicator;
    private View filterExpiredIndicator;  // CHANGED from filterUrgentIndicator

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
    private SharedPreferences preferences;

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

        // Initialize preferences
        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

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
        applyTheme();

        // Load category products
        loadCategoryProducts();

        Log.d(TAG, "CategoryDetailActivity created for: " + categoryName);
    }

    private void initializeViews() {
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
        TextView backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Filter Buttons - UPDATED: changed filterUrgent to filterExpired
        filterAll = findViewById(R.id.filterAll);
        filterSafe = findViewById(R.id.filterSafe);
        filterSoon = findViewById(R.id.filterSoon);
        filterExpired = findViewById(R.id.filterExpired);  // CHANGED
        filterAllIndicator = findViewById(R.id.filterAllIndicator);
        filterSafeIndicator = findViewById(R.id.filterSafeIndicator);
        filterSoonIndicator = findViewById(R.id.filterSoonIndicator);
        filterExpiredIndicator = findViewById(R.id.filterExpiredIndicator);  // CHANGED

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

    private void setupFilterButtons() {
        // Set All as active by default
        setActiveFilter(filterAll, filterAllIndicator);

        filterAll.setOnClickListener(v -> {
            setActiveFilter(filterAll, filterAllIndicator);
            currentFilter = "All";
            applyFilter();
        });

        filterSafe.setOnClickListener(v -> {
            setActiveFilter(filterSafe, filterSafeIndicator);
            currentFilter = "Safe";
            applyFilter();
        });

        filterSoon.setOnClickListener(v -> {
            setActiveFilter(filterSoon, filterSoonIndicator);
            currentFilter = "Soon";
            applyFilter();
        });

        // UPDATED: Changed from filterUrgent to filterExpired
        filterExpired.setOnClickListener(v -> {
            setActiveFilter(filterExpired, filterExpiredIndicator);
            currentFilter = "Expired";  // CHANGED from "Urgent"
            applyFilter();
        });
    }

    private void setActiveFilter(Button activeButton, View activeIndicator) {
        // Reset all - UPDATED: changed filterUrgent to filterExpired
        resetFilterButton(filterAll, filterAllIndicator);
        resetFilterButton(filterSafe, filterSafeIndicator);
        resetFilterButton(filterSoon, filterSoonIndicator);
        resetFilterButton(filterExpired, filterExpiredIndicator);  // CHANGED

        // Set active
        if (activeButton != null) {
            activeButton.setTextColor(Color.parseColor("#4361EE"));
            activeButton.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        if (activeIndicator != null) {
            activeIndicator.setVisibility(View.VISIBLE);
        }
    }

    private void resetFilterButton(Button button, View indicator) {
        if (button != null) {
            button.setTextColor(Color.parseColor("#999999"));
            button.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
        if (indicator != null) {
            indicator.setVisibility(View.GONE);
        }
    }

    // Setup view toggle between list and grid
    private void setupViewToggle() {
        if (listViewButton != null) {
            listViewButton.setOnClickListener(v -> {
                if (!isListView) {
                    isListView = true;
                    // Update icon colors
                    listViewButton.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4361EE")));
                    gridViewButton.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#999999")));
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
                    // Update icon colors
                    gridViewButton.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4361EE")));
                    listViewButton.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#999999")));
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
                        filteredProducts.add(product);  // 8+ days left
                    }
                    break;
                case "Soon":
                    if (expiryDate != null && !expiryDate.before(today) &&
                            expiryDate.getTime() - today.getTime() <= sevenDaysInMillis) {
                        filteredProducts.add(product);  // 1-7 days left
                    }
                    break;
                case "Expired":  // CHANGED from "Urgent"
                    if (expiryDate != null && expiryDate.before(today)) {
                        filteredProducts.add(product);  // Past expiry date
                    }
                    break;
            }
        }

        adapter.updateData(filteredProducts);
        Log.d(TAG, "Filter '" + currentFilter + "' applied - showing " + filteredProducts.size() + " products");
    }

    private void applyTheme() {
        int fabBackgroundColor = preferences.getInt("fab_background_color",
                getResources().getColor(R.color.color_fab_white));

        LinearLayout bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            bottomNavigation.setBackgroundColor(fabBackgroundColor);
        }
    }
}
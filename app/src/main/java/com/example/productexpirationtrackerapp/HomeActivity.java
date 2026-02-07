package com.example.productexpirationtrackerapp;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HomeActivity extends AppCompatActivity {

    // Views
    private RecyclerView productsRecyclerView, expiringRecyclerView;
    private ProductGridAdapter allProductsAdapter, expiringProductsAdapter;
    private EditText searchEditText;
    private CardView emptyStateCTA;
    private FloatingActionButton fab;
    private BottomNavigationView bottomNavigation;
    private LinearLayout emptyStateLayout, statsContainer, expiringSection;
    private TextView totalProductsCount, expiringSoonCount, expiredCount;
    private TextView notificationCount;
    private FrameLayout notificationBadge, notificationButton;
    private TextView categoryAll, categoryFood, categoryMedicine, categoryBeauty;
    private LinearLayout sortButton;
    private TextView viewAllExpiring;

    // Data lists
    private List<Product> allProducts;
    private List<Product> expiringProducts;
    private List<Product> filteredProducts;
    private AppDatabase database;

    // Filter state
    private String currentCategory = "All";
    private String currentSortBy = "Date"; // Date, Name, Category, Status
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeViews();
        setupDatabase();
        setupRecyclerViews();
        setupSearch();
        setupButtons();
        setupCategoryFilters();
        setupBottomNavigation();
        loadProducts();
    }

    private void initializeViews() {
        // RecyclerViews
        productsRecyclerView = findViewById(R.id.productsRecyclerView);
        expiringRecyclerView = findViewById(R.id.expiringRecyclerView);

        // Search
        searchEditText = findViewById(R.id.searchEditText);

        // Buttons
        emptyStateCTA = findViewById(R.id.emptyStateCTA);
        fab = findViewById(R.id.fab);
        notificationButton = findViewById(R.id.notificationButton);
        sortButton = findViewById(R.id.sortButton);
        viewAllExpiring = findViewById(R.id.viewAllExpiring);

        // Containers
        bottomNavigation = findViewById(R.id.bottomNavigation);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        statsContainer = findViewById(R.id.statsContainer);
        expiringSection = findViewById(R.id.expiringSection);

        // Stats
        totalProductsCount = findViewById(R.id.totalProductsCount);
        expiringSoonCount = findViewById(R.id.expiringSoonCount);
        expiredCount = findViewById(R.id.expiredCount);

        // Notification
        notificationBadge = findViewById(R.id.notificationBadge);
        notificationCount = findViewById(R.id.notificationCount);

        // Category chips
        categoryAll = findViewById(R.id.categoryAll);
        categoryFood = findViewById(R.id.categoryFood);
        categoryMedicine = findViewById(R.id.categoryMedicine);
        categoryBeauty = findViewById(R.id.categoryBeauty);
    }

    private void setupDatabase() {
        database = AppDatabase.getDatabase(this);
    }

    private void setupRecyclerViews() {
        allProducts = new ArrayList<>();
        expiringProducts = new ArrayList<>();
        filteredProducts = new ArrayList<>();

        // All Products Grid (2 columns)
        GridLayoutManager allGridLayout = new GridLayoutManager(this, 2);
        productsRecyclerView.setLayoutManager(allGridLayout);
        allProductsAdapter = new ProductGridAdapter(this, filteredProducts, product -> openProductDetail(product));
        productsRecyclerView.setAdapter(allProductsAdapter);

        // Expiring Products Horizontal
        GridLayoutManager expiringGridLayout = new GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false);
        expiringRecyclerView.setLayoutManager(expiringGridLayout);
        expiringProductsAdapter = new ProductGridAdapter(this, expiringProducts, product -> openProductDetail(product));
        expiringRecyclerView.setAdapter(expiringProductsAdapter);
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString();
                applyFiltersAndSort();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupButtons() {
        fab.setOnClickListener(v -> openAddProduct());

        if (emptyStateCTA != null) {
            emptyStateCTA.setOnClickListener(v -> openAddProduct());
        }

        notificationButton.setOnClickListener(v -> showNotifications());

        sortButton.setOnClickListener(v -> showSortDialog());

        viewAllExpiring.setOnClickListener(v -> {
            // Show all expiring products - could navigate to filtered view
            currentCategory = "All";
            updateCategorySelection();
            // Scroll to top
            productsRecyclerView.smoothScrollToPosition(0);
        });
    }

    private void setupCategoryFilters() {
        View.OnClickListener categoryClickListener = v -> {
            if (v == categoryAll) {
                currentCategory = "All";
            } else if (v == categoryFood) {
                currentCategory = "Food";
            } else if (v == categoryMedicine) {
                currentCategory = "Medicine";
            } else if (v == categoryBeauty) {
                currentCategory = "Beauty";
            }
            updateCategorySelection();
            applyFiltersAndSort();
        };

        categoryAll.setOnClickListener(categoryClickListener);
        categoryFood.setOnClickListener(categoryClickListener);
        categoryMedicine.setOnClickListener(categoryClickListener);
        categoryBeauty.setOnClickListener(categoryClickListener);
    }

    private void updateCategorySelection() {
        // Deselect all
        categoryAll.setSelected(false);
        categoryFood.setSelected(false);
        categoryMedicine.setSelected(false);
        categoryBeauty.setSelected(false);

        // Select current
        if (currentCategory.equals("All")) {
            categoryAll.setSelected(true);
        } else if (currentCategory.equals("Food")) {
            categoryFood.setSelected(true);
        } else if (currentCategory.equals("Medicine")) {
            categoryMedicine.setSelected(true);
        } else if (currentCategory.equals("Beauty")) {
            categoryBeauty.setSelected(true);
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    return true;
                } else if (itemId == R.id.nav_inventory) {
                    startActivity(new Intent(HomeActivity.this, InventoryActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_settings) {
                    startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }

                return false;
            }
        });
    }

    private void loadProducts() {
        new Thread(() -> {
            try {
                allProducts = database.productDao().getAllProducts();

                runOnUiThread(() -> {
                    separateExpiringProducts();
                    applyFiltersAndSort();
                    updateUI();
                    updateStats();
                    updateNotificationBadge();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(HomeActivity.this, "Error loading products: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void separateExpiringProducts() {
        expiringProducts.clear();

        for (Product product : allProducts) {
            long daysUntilExpiry = calculateDaysUntilExpiry(product.getExpiryDate());
            if (daysUntilExpiry >= 0 && daysUntilExpiry <= 7) {
                expiringProducts.add(product);
            }
        }

        // Sort expiring products by date (soonest first)
        Collections.sort(expiringProducts, (p1, p2) -> {
            long days1 = calculateDaysUntilExpiry(p1.getExpiryDate());
            long days2 = calculateDaysUntilExpiry(p2.getExpiryDate());
            return Long.compare(days1, days2);
        });
    }

    private void applyFiltersAndSort() {
        filteredProducts.clear();

        // Apply category filter
        for (Product product : allProducts) {
            boolean matchesCategory = currentCategory.equals("All") ||
                    (product.getCategory() != null &&
                            categorizeProduct(product.getCategory()).equals(currentCategory));

            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    product.getName().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                    (product.getCategory() != null &&
                            product.getCategory().toLowerCase().contains(currentSearchQuery.toLowerCase()));

            if (matchesCategory && matchesSearch) {
                filteredProducts.add(product);
            }
        }

        // Apply sorting
        sortProducts();

        allProductsAdapter.notifyDataSetChanged();
        expiringProductsAdapter.notifyDataSetChanged();
    }

    private String categorizeProduct(String category) {
        if (category == null) return "All";

        String lower = category.toLowerCase();

        // Food categories
        if (lower.contains("food") || lower.contains("dairy") || lower.contains("meat") ||
                lower.contains("fruit") || lower.contains("vegetable") || lower.contains("bread") ||
                lower.contains("snack") || lower.contains("frozen") || lower.contains("canned")) {
            return "Food";
        }

        // Medicine categories
        if (lower.contains("medicine") || lower.contains("medication") ||
                lower.contains("vitamin") || lower.contains("supplement") || lower.contains("pill")) {
            return "Medicine";
        }

        // Beauty categories
        if (lower.contains("beauty") || lower.contains("cosmetic") ||
                lower.contains("skincare") || lower.contains("makeup")) {
            return "Beauty";
        }

        return "All";
    }

    private void sortProducts() {
        switch (currentSortBy) {
            case "Date":
                Collections.sort(filteredProducts, (p1, p2) -> {
                    long days1 = calculateDaysUntilExpiry(p1.getExpiryDate());
                    long days2 = calculateDaysUntilExpiry(p2.getExpiryDate());
                    return Long.compare(days1, days2);
                });
                break;

            case "Name":
                Collections.sort(filteredProducts, (p1, p2) ->
                        p1.getName().compareToIgnoreCase(p2.getName()));
                break;

            case "Category":
                Collections.sort(filteredProducts, (p1, p2) -> {
                    String cat1 = p1.getCategory() != null ? p1.getCategory() : "";
                    String cat2 = p2.getCategory() != null ? p2.getCategory() : "";
                    return cat1.compareToIgnoreCase(cat2);
                });
                break;

            case "Status":
                Collections.sort(filteredProducts, (p1, p2) -> {
                    int status1 = getStatusPriority(calculateDaysUntilExpiry(p1.getExpiryDate()));
                    int status2 = getStatusPriority(calculateDaysUntilExpiry(p2.getExpiryDate()));
                    return Integer.compare(status1, status2);
                });
                break;
        }
    }

    private int getStatusPriority(long daysUntilExpiry) {
        if (daysUntilExpiry < 0) return 1; // Expired
        if (daysUntilExpiry == 0) return 2; // Today
        if (daysUntilExpiry <= 7) return 3; // Expiring soon
        return 4; // Fresh
    }

    private void showSortDialog() {
        String[] sortOptions = {"Date (Soonest First)", "Name (A-Z)", "Category", "Status (Urgent First)"};
        String[] sortValues = {"Date", "Name", "Category", "Status"};

        int currentIndex = 0;
        for (int i = 0; i < sortValues.length; i++) {
            if (sortValues[i].equals(currentSortBy)) {
                currentIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Sort Products")
                .setSingleChoiceItems(sortOptions, currentIndex, (dialog, which) -> {
                    currentSortBy = sortValues[which];
                    applyFiltersAndSort();
                    dialog.dismiss();
                    Toast.makeText(this, "Sorted by " + sortOptions[which], Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showNotifications() {
        // Count expiring/expired items
        int urgentCount = 0;
        for (Product product : allProducts) {
            long days = calculateDaysUntilExpiry(product.getExpiryDate());
            if (days <= 7) urgentCount++;
        }

        if (urgentCount == 0) {
            Toast.makeText(this, "No urgent notifications", Toast.LENGTH_SHORT).show();
        } else {
            String message = urgentCount + " product(s) expiring soon or expired!";
            new AlertDialog.Builder(this)
                    .setTitle("⚠️ Notifications")
                    .setMessage(message)
                    .setPositiveButton("View", (dialog, which) -> {
                        // Filter to show only expiring products
                        currentCategory = "All";
                        updateCategorySelection();
                        applyFiltersAndSort();
                    })
                    .setNegativeButton("Dismiss", null)
                    .show();
        }
    }

    private void updateNotificationBadge() {
        int urgentCount = 0;
        for (Product product : allProducts) {
            long days = calculateDaysUntilExpiry(product.getExpiryDate());
            if (days <= 7) urgentCount++;
        }

        if (urgentCount > 0) {
            notificationBadge.setVisibility(View.VISIBLE);
            notificationCount.setText(String.valueOf(Math.min(urgentCount, 99))); // Cap at 99
        } else {
            notificationBadge.setVisibility(View.GONE);
        }
    }

    private void updateUI() {
        if (allProducts == null || allProducts.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            productsRecyclerView.setVisibility(View.GONE);
            statsContainer.setVisibility(View.GONE);
            expiringSection.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            productsRecyclerView.setVisibility(View.VISIBLE);
            statsContainer.setVisibility(View.VISIBLE);

            // Show expiring section only if there are expiring products
            if (expiringProducts.isEmpty()) {
                expiringSection.setVisibility(View.GONE);
            } else {
                expiringSection.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateStats() {
        if (allProducts == null || allProducts.isEmpty()) {
            animateCount(totalProductsCount, 0);
            animateCount(expiringSoonCount, 0);
            animateCount(expiredCount, 0);
            return;
        }

        int total = allProducts.size();
        int expiringSoon = 0;
        int expired = 0;

        for (Product product : allProducts) {
            long daysUntilExpiry = calculateDaysUntilExpiry(product.getExpiryDate());

            if (daysUntilExpiry < 0) {
                expired++;
            } else if (daysUntilExpiry <= 7) {
                expiringSoon++;
            }
        }

        animateCount(totalProductsCount, total);
        animateCount(expiringSoonCount, expiringSoon);
        animateCount(expiredCount, expired);
    }

    private void animateCount(TextView textView, int targetCount) {
        ValueAnimator animator = ValueAnimator.ofInt(0, targetCount);
        animator.setDuration(1000);
        animator.setInterpolator(new DecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            textView.setText(String.valueOf(animation.getAnimatedValue()));
        });

        animator.start();
    }

    private long calculateDaysUntilExpiry(Date expiryDate) {
        if (expiryDate == null) {
            return 999;
        }

        try {
            Calendar expiryCal = Calendar.getInstance();
            expiryCal.setTime(expiryDate);
            expiryCal.set(Calendar.HOUR_OF_DAY, 0);
            expiryCal.set(Calendar.MINUTE, 0);
            expiryCal.set(Calendar.SECOND, 0);
            expiryCal.set(Calendar.MILLISECOND, 0);

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            long diffInMillis = expiryCal.getTimeInMillis() - today.getTimeInMillis();
            return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            return 999;
        }
    }

    private void openAddProduct() {
        Intent intent = new Intent(this, AddProductActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void openProductDetail(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("PRODUCT_ID", product.getId());
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }
}
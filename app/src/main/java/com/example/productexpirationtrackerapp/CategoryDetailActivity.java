package com.example.productexpirationtrackerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
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

    // Filter Chips
    private LinearLayout filterDropdownButton;
    private TextView filterCurrentLabel;

    // View Toggle Icons
    private ImageView listViewButton;
    private ImageView gridViewButton;
    private LinearLayout viewToggleContainer;
    private ImageView addButton;
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
        userRepository = new UserRepository(getApplication());

        // Initialize preferences
        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        // Initialize views
        initializeViews();

        // Setup filter chips
        setupFilterChips();

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
        ImageView backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Filter Chips
        filterDropdownButton = findViewById(R.id.filterDropdownButton);
        filterCurrentLabel   = findViewById(R.id.filterCurrentLabel);

        // View Toggle Icons
        listViewButton = findViewById(R.id.listViewButton);
        gridViewButton = findViewById(R.id.gridViewButton);
        viewToggleContainer = findViewById(R.id.viewToggleContainer);
        addButton = findViewById(R.id.addButton);

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

    private void setupFilterChips() {
        if (filterDropdownButton == null) return;

        // Show current filter label on startup
        updateFilterLabel();

        filterDropdownButton.setOnClickListener(v -> {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(this, filterDropdownButton);
            popup.getMenu().add(0, 0, 0, "All");
            popup.getMenu().add(0, 1, 1, "Safe");
            popup.getMenu().add(0, 2, 2, "Soon");
            popup.getMenu().add(0, 3, 3, "Expired");

            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0: currentFilter = "All";     break;
                    case 1: currentFilter = "Safe";    break;
                    case 2: currentFilter = "Soon";    break;
                    case 3: currentFilter = "Expired"; break;
                }
                updateFilterLabel();
                applyFilter();
                return true;
            });
            popup.show();
        });
    }

    private void updateFilterLabel() {
        if (filterCurrentLabel != null) {
            filterCurrentLabel.setText(currentFilter);
        }
    }

    // Setup view toggle between list and grid
    private void setupViewToggle() {
        if (listViewButton != null) {
            listViewButton.setOnClickListener(v -> {
                if (!isListView) {
                    isListView = true;
                    listViewButton.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4361EE")));
                    gridViewButton.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#999999")));
                    productsRecyclerView.setLayoutManager(new LinearLayoutManager(CategoryDetailActivity.this));
                    adapter.setGridView(false);
                }
            });
        }

        if (gridViewButton != null) {
            gridViewButton.setOnClickListener(v -> {
                if (isListView) {
                    isListView = false;
                    gridViewButton.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4361EE")));
                    listViewButton.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#999999")));
                    // Single-column LinearLayout so each card is full width (as per design sketch)
                    productsRecyclerView.setLayoutManager(new LinearLayoutManager(CategoryDetailActivity.this));
                    adapter.setGridView(true);
                }
            });
        }
    }

    private void setupBottomNavigation() {
        // Set all as inactive (we're in a detail page)
        setInactiveNavItem(navProfile, navProfileIcon, navProfileText, navProfileIndicator);
        setInactiveNavItem(navProducts, navProductsIcon, navProductsText, navProductsIndicator);
        setInactiveNavItem(navSettings, navSettingsIcon, navSettingsText, navSettingsIndicator);

        // FAB click — navigate to Add Product
        if (addButton != null) {
            addButton.setOnClickListener(v -> {
                Intent intent = new Intent(CategoryDetailActivity.this, AddProductActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

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
        String theme = preferences.getString("color_theme", "white");
        int iconColor = "black".equals(theme) ? Color.WHITE : Color.parseColor("#666666");
        if (icon != null) {
            icon.setImageTintList(android.content.res.ColorStateList.valueOf(iconColor));
        }
        if (text != null) {
            text.setTextColor(iconColor);
            text.setAlpha(1.0f);
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

        adapter = new ProductAdapter(filteredProducts);

        // Card tap — disabled, do nothing (use ⋮ menu to take action)
        adapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // No action on plain card tap
            }

            @Override
            public void onItemLongClick(int position) {
                // No action on long press
            }
        });

        // 3-dot ⋮ menu
        adapter.setOnMenuClickListener(new ProductAdapter.OnMenuClickListener() {
            @Override
            public void onEditClick(int position) {
                // Opens the existing ProductDetailActivity (same screen as before)
                Product product = filteredProducts.get(position);
                Intent intent = new Intent(CategoryDetailActivity.this, ProductDetailActivity.class);
                intent.putExtra("product_id", product.getId());
                intent.putExtra("product_name", product.getName());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                intent.putExtra("expiry_date", sdf.format(product.getExpiryDate()));
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }

            @Override
            public void onDeleteClick(int position) {
                Product product = filteredProducts.get(position);
                new androidx.appcompat.app.AlertDialog.Builder(CategoryDetailActivity.this)
                        .setTitle("Delete Product")
                        .setMessage("Delete \"" + product.getName() + "\" permanently? This cannot be undone.")
                        .setPositiveButton("DELETE", (dialog, which) -> {
                            productViewModel.delete(product);
                            Toast.makeText(CategoryDetailActivity.this,
                                    "Deleted: " + product.getName(), Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("CANCEL", null)
                        .show();
            }
        });

        if (productsRecyclerView != null) {
            productsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
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
                case "Expired":
                    if (expiryDate != null && expiryDate.before(today)) {
                        filteredProducts.add(product);  // Past expiry date
                    }
                    break;
            }
        }

        adapter.updateData(filteredProducts);
        Log.d(TAG, "Filter '" + currentFilter + "' applied - showing " + filteredProducts.size() + " products");
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyTheme();
    }

    private void applyTheme() {
        // Read theme from database first (same as ProductListActivity), fall back to SharedPreferences
        User user = userRepository.getUserSync();
        String theme;
        if (user != null && user.getColorTheme() != null && !user.getColorTheme().isEmpty()) {
            theme = user.getColorTheme();
        } else {
            theme = preferences.getString("color_theme", "white"); // correct key: "color_theme"
        }
        applyThemeColors(theme);
    }

    private void applyThemeColors(String theme) {
        boolean isDark = "black".equals(theme);
        float dp = getResources().getDisplayMetrics().density;

        // --- Exact same color logic as ProductListActivity ---
        int mainBg        = isDark ? Color.parseColor("#121212")  : Color.parseColor("#F5F5F5");
        int cardBg        = isDark ? Color.parseColor("#1E1E1E")  : Color.WHITE;
        int navBg         = isDark ? ContextCompat.getColor(this, R.color.color_nav_background_black)
                : ContextCompat.getColor(this, R.color.color_nav_background_white);
        int fabBg         = isDark ? Color.parseColor("#1E1E1E")  // dark: near-black, NOT purple
                : ContextCompat.getColor(this, R.color.color_fab_white);
        int primaryText   = isDark ? Color.WHITE                  : Color.parseColor("#1A1E2C");
        int secondaryText = isDark ? Color.LTGRAY                 : Color.parseColor("#8A8F9E");
        int navIconColor  = isDark ? Color.WHITE                  : Color.WHITE; // nav always dark bg

        // Root background
        android.view.View mainLayout = findViewById(R.id.mainLayout);
        if (mainLayout != null) mainLayout.setBackgroundColor(mainBg);

        // Back arrow tint
        ImageView backBtn = findViewById(R.id.backButton);
        if (backBtn != null)
            backBtn.setImageTintList(android.content.res.ColorStateList.valueOf(primaryText));

        // Header text
        TextView categoryTitle = findViewById(R.id.categoryTitle);
        if (categoryTitle != null) categoryTitle.setTextColor(primaryText);
        TextView itemCount = findViewById(R.id.categoryItemCount);
        if (itemCount != null) itemCount.setTextColor(secondaryText);

        // Stats card background
        LinearLayout statsCard = findViewById(R.id.statsCard);
        if (statsCard != null) {
            android.graphics.drawable.GradientDrawable statsBg = new android.graphics.drawable.GradientDrawable();
            statsBg.setColor(cardBg);
            statsBg.setCornerRadius(24 * dp);
            if (!isDark) statsBg.setStroke(1, Color.parseColor("#E8EAF0"));
            statsCard.setBackground(statsBg);
        }

        // Filter label
        TextView filterLabel = findViewById(R.id.filterLabel);
        if (filterLabel != null) filterLabel.setTextColor(primaryText);

        // Filter dropdown text + arrow + button background
        TextView filterCurrentLabel = findViewById(R.id.filterCurrentLabel);
        if (filterCurrentLabel != null) filterCurrentLabel.setTextColor(primaryText);
        ImageView filterArrow = findViewById(R.id.filterDropdownArrow);
        if (filterArrow != null)
            filterArrow.setImageTintList(android.content.res.ColorStateList.valueOf(primaryText));
        LinearLayout filterBtn = findViewById(R.id.filterDropdownButton);
        if (filterBtn != null) {
            android.graphics.drawable.GradientDrawable filterBg = new android.graphics.drawable.GradientDrawable();
            filterBg.setColor(cardBg);
            filterBg.setCornerRadius(32 * dp);
            filterBg.setStroke(1, isDark ? Color.parseColor("#444444") : Color.parseColor("#DDDDDD"));
            filterBtn.setBackground(filterBg);
        }

        // View toggle container — same dark pill as the filter button
        if (viewToggleContainer != null) {
            android.graphics.drawable.GradientDrawable toggleBg = new android.graphics.drawable.GradientDrawable();
            toggleBg.setColor(cardBg);
            toggleBg.setCornerRadius(32 * dp);
            toggleBg.setStroke(1, isDark ? Color.parseColor("#444444") : Color.parseColor("#DDDDDD"));
            viewToggleContainer.setBackground(toggleBg);
        }

        // Nav background — rounded top corners
        LinearLayout bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            android.graphics.drawable.GradientDrawable navRounded = new android.graphics.drawable.GradientDrawable();
            navRounded.setColor(navBg);
            float[] navRadii = {48f, 48f, 48f, 48f, 0f, 0f, 0f, 0f};
            navRounded.setCornerRadii(navRadii);
            bottomNavigation.setBackground(navRounded);
        }

        // Nav icons + text
        ImageView[] navIcons = {navProfileIcon, navProductsIcon, navSettingsIcon};
        TextView[] navTexts  = {navProfileText, navProductsText, navSettingsText};
        for (ImageView icon : navIcons)
            if (icon != null) icon.setImageTintList(android.content.res.ColorStateList.valueOf(navIconColor));
        for (TextView text : navTexts)
            if (text != null) text.setTextColor(navIconColor);

        // FAB — use same color as ProductListActivity (not from prefs which may have stale purple)
        if (addButton != null) {
            addButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(fabBg));
            addButton.setImageTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
        }
    }
}
package com.example.productexpirationtrackerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.widget.HorizontalScrollView;

import java.util.ArrayList;
import java.util.List;

public class ProductListActivity extends AppCompatActivity {

    private static final String TAG = "ProductListDebug";

    // UI Components
    private ImageView logoImageView;
    private TextView appNameTextView;
    private TextView hiUserTextView;
    private TextView welcomeDescriptionTextView;
    private TextView categoryLabelTextView;
    private ImageView addButton;
    private TextView productCountText;
    private RecyclerView productListView;  // CHANGED: ListView -> RecyclerView
    private View rootView;
    private LinearLayout bottomNavigation;
    private LinearLayout headerLayout;
    private EditText searchEditText;
    private TextView searchEmoji;
    private ImageView clearSearchButton;
    private HorizontalScrollView categoryScrollView;

    // Category filter buttons
    private TextView categoryAllButton, categoryFoodButton, categoryMedicineButton, categoryDrinksButton, categoryOtherButton;

    // Bottom Navigation
    private LinearLayout navProfile, navProducts, navSettings;
    private ImageView navProfileIcon, navProductsIcon, navSettingsIcon;
    private TextView navProfileText, navProductsText, navSettingsText;

    // Data
    private ArrayList<Product> productList;
    private ArrayList<Product> allProducts;
    private ProductRecyclerAdapter adapter;  // CHANGED: ProductListAdapter -> ProductListRecyclerAdapter
    private SharedPreferences preferences;
    private UserRepository userRepository;
    private ProductViewModel productViewModel;

    // Current category tracking
    private String currentCategory = "All";
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "ProductListActivity onCreate started");

        try {
            setContentView(R.layout.activity_product_list);
            Log.d(TAG, "Layout set successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting content view: " + e.getMessage());
            Toast.makeText(this, "Layout error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize repositories
        userRepository = new UserRepository(getApplication());

        // Initialize ViewModel
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        // Get preferences
        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        Log.d(TAG, "Preferences loaded");

        // Get root view for theme application
        rootView = getWindow().getDecorView().getRootView();

        // Initialize ALL UI components
        initializeViews();

        // Initialize category buttons
        initializeCategoryButtons();

        // Initialize bottom navigation
        initializeBottomNavigation();

        // Setup search functionality
        setupSearchFunctionality();

        // Apply theme from database
        applyThemeFromDatabase();

        // Setup product list (load from database)
        setupProductList();

        // Setup button click listeners
        setupClickListeners();

        // Load all products by default
        loadAllProducts();

        // Update category counts
        updateCategoryCounts();

        Log.d(TAG, "ProductListActivity setup complete");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reapply theme
        applyThemeFromDatabase();

        // Update welcome text with user name
        updateWelcomeText();

        Log.d(TAG, "onResume called, theme reapplied and welcome text updated");
    }

    private void updateWelcomeText() {
        if (hiUserTextView != null) {
            String userName = preferences.getString("user_name", "User");
            hiUserTextView.setText("Hi, " + userName + "! \uD83D\uDC4B");
            Log.d(TAG, "Welcome text updated for user: " + userName);
        }
    }

    private void applyThemeFromDatabase() {
        // Get user from database to get theme
        User user = userRepository.getUserSync();

        if (user != null) {
            String theme = user.getColorTheme();
            Log.d(TAG, "Applying theme from database: " + theme);

            // Apply theme using ThemeUtils
            ThemeUtils.applyTheme(this, theme);

            // Apply additional custom theme colors
            applyCustomThemeColors(theme);
        } else {
            // Fallback to SharedPreferences
            String theme = preferences.getString("color_theme", "white");
            Log.d(TAG, "No user in DB, applying theme from SharedPreferences: " + theme);

            ThemeUtils.applyTheme(this, theme);
            applyCustomThemeColors(theme);
        }
    }

    private void applyCustomThemeColors(String theme) {
        int primaryColor;
        int textColor;
        int backgroundColor;
        int fabBackgroundColor = 0;

        // Get colors based on theme
        switch (theme) {
            case "green":
                primaryColor = getResources().getColor(R.color.color_primary_green);
                textColor = getResources().getColor(R.color.color_text_green);
                backgroundColor = getResources().getColor(R.color.color_background_green);
                fabBackgroundColor = getResources().getColor(R.color.color_fab_green);
                break;
            case "blue":
                primaryColor = getResources().getColor(R.color.color_primary_blue);
                textColor = getResources().getColor(R.color.color_text_blue);
                backgroundColor = getResources().getColor(R.color.color_background_blue);
                fabBackgroundColor = getResources().getColor(R.color.color_fab_blue);
                break;
            case "pink":
                primaryColor = getResources().getColor(R.color.color_primary_pink);
                textColor = getResources().getColor(R.color.color_text_pink);
                backgroundColor = getResources().getColor(R.color.color_background_pink);
                fabBackgroundColor = getResources().getColor(R.color.color_fab_pink);
                break;
            case "purple":
                primaryColor = getResources().getColor(R.color.color_primary_purple);
                textColor = getResources().getColor(R.color.color_text_purple);
                backgroundColor = getResources().getColor(R.color.color_background_purple);
                fabBackgroundColor = getResources().getColor(R.color.color_fab_purple);
                break;
            case "black":
                primaryColor = getResources().getColor(R.color.color_primary_black);
                textColor = getResources().getColor(R.color.color_text_black);
                backgroundColor = getResources().getColor(R.color.color_background_black);
                fabBackgroundColor = getResources().getColor(R.color.color_fab_black);
                break;
            default: // white/default
                primaryColor = getResources().getColor(R.color.color_primary_white);
                textColor = getResources().getColor(R.color.color_text_white);
                backgroundColor = getResources().getColor(R.color.color_background_white);
                fabBackgroundColor = getResources().getColor(R.color.color_fab_white);
                break;
        }

        // Store FAB color for category buttons
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("fab_background_color", fabBackgroundColor);
        editor.apply();

        // Apply colors to various components
        if (rootView != null) {
            rootView.setBackgroundColor(backgroundColor);
        }

        if (headerLayout != null) {
            headerLayout.setBackgroundColor(primaryColor);
        }

        if (bottomNavigation != null) {
            // Note: Bottom navigation gradient will be applied via drawable
        }

        Log.d(TAG, "Custom theme colors applied");
    }

    private void initializeViews() {
        try {
            // Header components
            logoImageView = findViewById(R.id.logoImageView);
            appNameTextView = findViewById(R.id.appNameTextView);
            hiUserTextView = findViewById(R.id.hiUserTextView);
            welcomeDescriptionTextView = findViewById(R.id.welcomeDescriptionTextView);
            categoryLabelTextView = findViewById(R.id.categoryLabelTextView);
            headerLayout = findViewById(R.id.headerLayout);

            // Search components
            searchEditText = findViewById(R.id.searchEditText);
            searchEmoji = findViewById(R.id.searchEmoji);
            clearSearchButton = findViewById(R.id.clearSearchButton);
            categoryScrollView = findViewById(R.id.categoryScrollView);

            // Product list components
            productCountText = findViewById(R.id.productCountText);
            productListView = findViewById(R.id.productListView);

            // Action button
            addButton = findViewById(R.id.addButton);

            // Bottom navigation
            bottomNavigation = findViewById(R.id.bottomNavigation);

            Log.d(TAG, "All views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
            throw e;
        }
    }

    private void initializeCategoryButtons() {
        try {
            categoryAllButton = findViewById(R.id.categoryAllButton);
            categoryFoodButton = findViewById(R.id.categoryFoodButton);
            categoryMedicineButton = findViewById(R.id.categoryMedicineButton);
            categoryDrinksButton = findViewById(R.id.categoryDrinksButton);
            categoryOtherButton = findViewById(R.id.categoryOtherButton);

            // Set click listeners for category buttons
            if (categoryAllButton != null) {
                categoryAllButton.setOnClickListener(v -> {
                    currentCategory = "All";
                    loadAllProducts();
                    setActiveCategoryButton(categoryAllButton, getFabColorFromPreferences());
                    Log.d(TAG, "Category changed to: All");
                });
            }

            if (categoryFoodButton != null) {
                categoryFoodButton.setOnClickListener(v -> {
                    currentCategory = "Food";
                    loadProductsByCategory("Food");
                    setActiveCategoryButton(categoryFoodButton, getFabColorFromPreferences());
                    Log.d(TAG, "Category changed to: Food");
                });
            }

            if (categoryMedicineButton != null) {
                categoryMedicineButton.setOnClickListener(v -> {
                    currentCategory = "Medicine";
                    loadProductsByCategory("Medicine");
                    setActiveCategoryButton(categoryMedicineButton, getFabColorFromPreferences());
                    Log.d(TAG, "Category changed to: Medicine");
                });
            }

            if (categoryDrinksButton != null) {
                categoryDrinksButton.setOnClickListener(v -> {
                    currentCategory = "Drinks";
                    loadProductsByCategory("Drinks");
                    setActiveCategoryButton(categoryDrinksButton, getFabColorFromPreferences());
                    Log.d(TAG, "Category changed to: Drinks");
                });
            }

            if (categoryOtherButton != null) {
                categoryOtherButton.setOnClickListener(v -> {
                    currentCategory = "Other";
                    loadProductsByCategory("Other");
                    setActiveCategoryButton(categoryOtherButton, getFabColorFromPreferences());
                    Log.d(TAG, "Category changed to: Other");
                });
            }

            Log.d(TAG, "Category buttons initialized");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing category buttons: " + e.getMessage());
        }
    }

    private void initializeBottomNavigation() {
        try {
            navProfile = findViewById(R.id.navProfile);
            navProducts = findViewById(R.id.navProducts);
            navSettings = findViewById(R.id.navSettings);

            navProfileIcon = findViewById(R.id.navProfileIcon);
            navProductsIcon = findViewById(R.id.navProductsIcon);
            navSettingsIcon = findViewById(R.id.navSettingsIcon);

            navProfileText = findViewById(R.id.navProfileText);
            navProductsText = findViewById(R.id.navProductsText);
            navSettingsText = findViewById(R.id.navSettingsText);

            // Set click listeners - REMOVED ProfileActivity
            if (navProfile != null) {
                navProfile.setOnClickListener(v -> {
                    Toast.makeText(ProductListActivity.this, "Profile feature coming soon!", Toast.LENGTH_SHORT).show();
                });
            }

            if (navSettings != null) {
                navSettings.setOnClickListener(v -> {
                    Intent settingsIntent = new Intent(ProductListActivity.this, SettingsActivity.class);
                    startActivity(settingsIntent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                });
            }

            Log.d(TAG, "Bottom navigation initialized");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing bottom navigation: " + e.getMessage());
        }
    }

    private void setupSearchFunctionality() {
        if (searchEditText != null && clearSearchButton != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = s.toString();
                    filterProducts(currentSearchQuery);

                    // Show/hide clear button
                    if (s.length() > 0) {
                        clearSearchButton.setVisibility(View.VISIBLE);
                    } else {
                        clearSearchButton.setVisibility(View.GONE);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            clearSearchButton.setOnClickListener(v -> {
                searchEditText.setText("");
                clearSearchButton.setVisibility(View.GONE);
            });

            Log.d(TAG, "Search functionality setup complete");
        }
    }

    private void filterProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            // Show all products in current category
            if (currentCategory.equals("All")) {
                updateProductList(allProducts);
            } else {
                List<Product> filteredByCategory = new ArrayList<>();
                for (Product product : allProducts) {
                    if (product.getCategory().equals(currentCategory)) {
                        filteredByCategory.add(product);
                    }
                }
                updateProductList(filteredByCategory);
            }
        } else {
            // Filter by search query
            List<Product> filtered = new ArrayList<>();
            String lowerQuery = query.toLowerCase();

            for (Product product : allProducts) {
                boolean matchesCategory = currentCategory.equals("All") ||
                        product.getCategory().equals(currentCategory);
                boolean matchesQuery = product.getName().toLowerCase().contains(lowerQuery);

                if (matchesCategory && matchesQuery) {
                    filtered.add(product);
                }
            }
            updateProductList(filtered);
        }
    }

    // CHANGED: Modified setupProductList to work with RecyclerView
    private void setupProductList() {
        try {
            productList = new ArrayList<>();
            allProducts = new ArrayList<>();

            // Create your existing ListView adapter
            ProductListAdapter listAdapter = new ProductListAdapter(this, productList);

            // Wrap it for RecyclerView
            adapter = new ProductRecyclerAdapter(listAdapter);

            // Setup RecyclerView
            productListView.setLayoutManager(new LinearLayoutManager(this));
            productListView.setAdapter(adapter);

            // Item click listener
            adapter.setOnItemClickListener(position -> {
                if (position < productList.size()) {
                    Product product = productList.get(position);
                    Intent productDetailIntent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
                    productDetailIntent.putExtra("product_id", product.getId());
                    startActivityForResult(productDetailIntent, 100);
                }
            });

            // Long click listener for delete
            adapter.setOnItemLongClickListener(position -> {
                if (position < productList.size()) {
                    Product product = productList.get(position);
                    productViewModel.delete(product);
                    Toast.makeText(ProductListActivity.this,
                            "Removed: " + product.getName(),
                            Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Item long clicked and removed from database: " + product.getName());
                }
                return true;
            });

            Log.d(TAG, "Product list setup complete");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up product list: " + e.getMessage());
            Toast.makeText(this, "Error setting up product list", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        // Add button click listener
        if (addButton != null) {
            addButton.setOnClickListener(v -> {
                Intent addProductIntent = new Intent(ProductListActivity.this, AddProductActivity.class);
                startActivityForResult(addProductIntent, 200);
            });
        }
    }

    private void setActiveCategoryButton(TextView activeButton, int fabColor) {
        // Reset all buttons to lighter FAB color
        float[] hsv = new float[3];
        Color.colorToHSV(fabColor, hsv);
        hsv[2] = 0.8f;
        int lighterFabColor = Color.HSVToColor(hsv);

        if (categoryAllButton != null) {
            categoryAllButton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(lighterFabColor));
        }
        if (categoryFoodButton != null) {
            categoryFoodButton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(lighterFabColor));
        }
        if (categoryMedicineButton != null) {
            categoryMedicineButton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(lighterFabColor));
        }
        if (categoryDrinksButton != null) {
            categoryDrinksButton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(lighterFabColor));
        }
        if (categoryOtherButton != null) {
            categoryOtherButton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(lighterFabColor));
        }

        // Set active button to solid FAB color
        if (activeButton != null) {
            activeButton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(fabColor));
        }
    }

    private int getFabColorFromPreferences() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return prefs.getInt("fab_background_color",
                getResources().getColor(R.color.color_fab_white));
    }

    private void updateCategoryCounts() {
        productViewModel.getAllProducts().observe(this, new Observer<List<Product>>() {
            @Override
            public void onChanged(List<Product> products) {
                if (products != null) {
                    int allCount = products.size();
                    int foodCount = 0;
                    int medicineCount = 0;
                    int drinksCount = 0;
                    int otherCount = 0;

                    for (Product product : products) {
                        String category = product.getCategory();
                        if (category != null) {
                            switch (category) {
                                case "Food":
                                    foodCount++;
                                    break;
                                case "Medicine":
                                    medicineCount++;
                                    break;
                                case "Drinks":
                                    drinksCount++;
                                    break;
                                case "Other":
                                    otherCount++;
                                    break;
                            }
                        }
                    }

                    // Update button texts with counts
                    if (categoryAllButton != null) {
                        categoryAllButton.setText("All\n" + allCount + " items");
                    }
                    if (categoryFoodButton != null) {
                        categoryFoodButton.setText("🍎\nFood\n" + foodCount + " items");
                    }
                    if (categoryMedicineButton != null) {
                        categoryMedicineButton.setText("💊\nMedicine\n" + medicineCount + " items");
                    }
                    if (categoryDrinksButton != null) {
                        categoryDrinksButton.setText("🥤\nDrinks\n" + drinksCount + " items");
                    }
                    if (categoryOtherButton != null) {
                        categoryOtherButton.setText("📦\nOther\n" + otherCount + " items");
                    }
                }
            }
        });
    }

    private void loadAllProducts() {
        productViewModel.getAllProducts().observe(this, new Observer<List<Product>>() {
            @Override
            public void onChanged(List<Product> products) {
                if (products != null) {
                    allProducts.clear();
                    allProducts.addAll(products);

                    if (currentSearchQuery != null && !currentSearchQuery.isEmpty()) {
                        filterProducts(currentSearchQuery);
                    } else {
                        updateProductList(products);
                    }
                }
            }
        });
    }

    private void loadProductsByCategory(String category) {
        productViewModel.getProductsByCategory(category).observe(this, new Observer<List<Product>>() {
            @Override
            public void onChanged(List<Product> products) {
                if (products != null) {
                    allProducts.clear();
                    allProducts.addAll(products);

                    if (currentSearchQuery != null && !currentSearchQuery.isEmpty()) {
                        filterProducts(currentSearchQuery);
                    } else {
                        updateProductList(products);
                    }
                }
            }
        });
    }

    private void updateProductList(List<Product> products) {
        if (products != null) {
            productList.clear();
            productList.addAll(products);
            adapter.updateData(products);
            updateProductCount();

            Log.d(TAG, "Updated product list with " + products.size() + " products (Category: " + currentCategory + ")");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200 && resultCode == RESULT_OK) {
            Toast.makeText(this, "Product added!", Toast.LENGTH_SHORT).show();
        }

        if (requestCode == 100 && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("deleted_product_id")) {
                int productId = data.getIntExtra("deleted_product_id", -1);

                if (productId != -1) {
                    productViewModel.deleteById(productId);
                    Toast.makeText(ProductListActivity.this,
                            "Product deleted",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void updateProductCount() {
        if (productCountText != null) {
            String searchInfo = "";
            if (currentSearchQuery != null && !currentSearchQuery.isEmpty()) {
                searchInfo = " (searching: \"" + currentSearchQuery + "\")";
            }

            if (productList.isEmpty()) {
                productCountText.setText("No products found" + searchInfo);
            } else {
                productCountText.setText(productList.size() + " product" +
                        (productList.size() == 1 ? "" : "s") +
                        " in " + currentCategory + searchInfo);
            }
            Log.d(TAG, "Updated product count: " + productList.size());
        }
    }
}
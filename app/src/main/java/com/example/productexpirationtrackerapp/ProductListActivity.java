package com.example.productexpirationtrackerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductListActivity extends AppCompatActivity {

    private static final String TAG = "ProductListDebug";

    // UI Components
    private TextView titleTextView;
    private FloatingActionButton addButton;
    private TextView productCountText;
    private RecyclerView productRecyclerView; // Changed from ListView
    private LinearLayout emptyStateLayout;
    private View rootView;
    private LinearLayout bottomNavigation;
    private LinearLayout headerLayout;
    private ImageView viewToggleButton; // New: View toggle button

    // Category filter buttons
    private Button categoryAllButton, categoryFoodButton, categoryMedicineButton, categoryDrinksButton, categoryOtherButton;

    // Bottom Navigation
    private LinearLayout navProfile, navProducts, navSettings;
    private ImageView navProfileIcon, navProductsIcon, navSettingsIcon;
    private TextView navProfileText, navProductsText, navSettingsText;

    // Data
    private ArrayList<Product> productList;
    private ProductRecyclerAdapter adapter; // Changed adapter type
    private SharedPreferences preferences;
    private UserRepository userRepository;
    private ProductViewModel productViewModel;

    // Current category tracking
    private String currentCategory = "All";

    // View type tracking (grid or list)
    private boolean isGridView = true; // Default to grid view

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

        // Apply theme from database
        applyThemeFromDatabase();

        // Setup product RecyclerView (replaces setupProductList)
        setupProductRecyclerView();

        // Setup button click listeners
        setupClickListeners();

        // Load all products by default
        loadAllProducts();

        Log.d(TAG, "ProductListActivity setup complete");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update user name if changed
        String userName = preferences.getString("user_name", "User");
        if (titleTextView != null) {
            titleTextView.setText("📦 " + userName + "'s Products");
        }

        // Reapply theme
        applyThemeFromDatabase();

        Log.d(TAG, "onResume called, title updated and theme reapplied");
    }

    private void initializeViews() {
        try {
            // Header components
            headerLayout = findViewById(R.id.headerLayout);
            titleTextView = findViewById(R.id.titleTextView);
            viewToggleButton = findViewById(R.id.viewToggleButton); // New toggle button

            // Main components
            productCountText = findViewById(R.id.productCountText);
            productRecyclerView = findViewById(R.id.productRecyclerView); // Changed from ListView
            emptyStateLayout = findViewById(R.id.emptyStateLayout);
            addButton = findViewById(R.id.addButton);

            // Navigation
            bottomNavigation = findViewById(R.id.bottomNavigation);

            Log.d(TAG, "All views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
        }
    }

    private void initializeCategoryButtons() {
        categoryAllButton = findViewById(R.id.categoryAllButton);
        categoryFoodButton = findViewById(R.id.categoryFoodButton);
        categoryMedicineButton = findViewById(R.id.categoryMedicineButton);
        categoryDrinksButton = findViewById(R.id.categoryDrinksButton);
        categoryOtherButton = findViewById(R.id.categoryOtherButton);

        Log.d(TAG, "Category buttons initialized");
    }

    private void initializeBottomNavigation() {
        navProfile = findViewById(R.id.navProfile);
        navProducts = findViewById(R.id.navProducts);
        navSettings = findViewById(R.id.navSettings);

        navProfileIcon = findViewById(R.id.navProfileIcon);
        navProductsIcon = findViewById(R.id.navProductsIcon);
        navSettingsIcon = findViewById(R.id.navSettingsIcon);

        navProfileText = findViewById(R.id.navProfileText);
        navProductsText = findViewById(R.id.navProductsText);
        navSettingsText = findViewById(R.id.navSettingsText);

        Log.d(TAG, "Bottom navigation initialized");
    }

    private void setupProductRecyclerView() {
        productList = new ArrayList<>();

        // Setup RecyclerView with GridLayoutManager (default)
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2); // 2 columns
        productRecyclerView.setLayoutManager(gridLayoutManager);

        // Initialize adapter
        adapter = new ProductRecyclerAdapter(this, productList);
        productRecyclerView.setAdapter(adapter);

        // Set click listeners
        adapter.setOnProductClickListener((product, position) -> {
            Log.d(TAG, "Opening product details: " + product.getName());
            openProductDetail(product);
        });

        adapter.setOnProductLongClickListener((product, position) -> {
            Log.d(TAG, "Long click - deleting: " + product.getName());
            productViewModel.delete(product);
            Toast.makeText(this, "Removed: " + product.getName(), Toast.LENGTH_SHORT).show();
        });

        Log.d(TAG, "RecyclerView setup complete");
    }

    private void setupClickListeners() {
        // Add button click
        if (addButton != null) {
            addButton.setOnClickListener(v -> {
                Log.d(TAG, "Add button clicked");
                Intent intent = new Intent(ProductListActivity.this, AddProductActivity.class);
                startActivityForResult(intent, 200);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        // View Toggle Button Click
        if (viewToggleButton != null) {
            viewToggleButton.setOnClickListener(v -> toggleView());
        }

        // Category Filter Buttons
        if (categoryAllButton != null) {
            categoryAllButton.setOnClickListener(v -> {
                currentCategory = "All";
                setActiveCategoryButton(categoryAllButton);
                loadAllProducts();
            });
        }

        if (categoryFoodButton != null) {
            categoryFoodButton.setOnClickListener(v -> {
                currentCategory = "Food";
                setActiveCategoryButton(categoryFoodButton);
                loadProductsByCategory("Food");
            });
        }

        if (categoryMedicineButton != null) {
            categoryMedicineButton.setOnClickListener(v -> {
                currentCategory = "Medicine";
                setActiveCategoryButton(categoryMedicineButton);
                loadProductsByCategory("Medicine");
            });
        }

        if (categoryDrinksButton != null) {
            categoryDrinksButton.setOnClickListener(v -> {
                currentCategory = "Drinks";
                setActiveCategoryButton(categoryDrinksButton);
                loadProductsByCategory("Drinks");
            });
        }

        if (categoryOtherButton != null) {
            categoryOtherButton.setOnClickListener(v -> {
                currentCategory = "Other";
                setActiveCategoryButton(categoryOtherButton);
                loadProductsByCategory("Other");
            });
        }

        // Bottom Navigation Click Listeners
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Log.d(TAG, "Profile navigation clicked");
                Intent intent = new Intent(ProductListActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            });
        }

        if (navProducts != null) {
            navProducts.setOnClickListener(v -> {
                Log.d(TAG, "Products navigation clicked - already on this screen");
                if (productRecyclerView != null) {
                    productRecyclerView.smoothScrollToPosition(0);
                }
            });
        }

        if (navSettings != null) {
            navSettings.setOnClickListener(v -> {
                Log.d(TAG, "Settings navigation clicked");
                Intent intent = new Intent(ProductListActivity.this, SettingsActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            });
        }
    }

    // Toggle between Grid and List view
    private void toggleView() {
        isGridView = !isGridView;

        if (isGridView) {
            // Switch to Grid View
            GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
            productRecyclerView.setLayoutManager(gridLayoutManager);
            adapter.setViewType(0); // VIEW_TYPE_GRID
            viewToggleButton.setImageResource(android.R.drawable.ic_menu_view);
            Log.d(TAG, "Switched to Grid View");
        } else {
            // Switch to List View
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            productRecyclerView.setLayoutManager(linearLayoutManager);
            adapter.setViewType(1); // VIEW_TYPE_LIST
            viewToggleButton.setImageResource(android.R.drawable.ic_menu_agenda);
            Log.d(TAG, "Switched to List View");
        }
    }

    private void setActiveCategoryButton(Button activeButton) {
        // Reset all buttons to semi-transparent
        categoryAllButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0x80FFFFFF));
        categoryAllButton.setTextColor(0xFFFFFFFF);

        categoryFoodButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0x80FFFFFF));
        categoryFoodButton.setTextColor(0xFFFFFFFF);

        categoryMedicineButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0x80FFFFFF));
        categoryMedicineButton.setTextColor(0xFFFFFFFF);

        categoryDrinksButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0x80FFFFFF));
        categoryDrinksButton.setTextColor(0xFFFFFFFF);

        categoryOtherButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0x80FFFFFF));
        categoryOtherButton.setTextColor(0xFFFFFFFF);

        // Set active button to solid white with dark text
        activeButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0xFFFFFFFF));
        activeButton.setTextColor(0xFF333333);
    }

    private void loadAllProducts() {
        productViewModel.getAllProducts().observe(this, new Observer<List<Product>>() {
            @Override
            public void onChanged(List<Product> products) {
                updateProductList(products);
            }
        });
    }

    private void loadProductsByCategory(String category) {
        productViewModel.getProductsByCategory(category).observe(this, new Observer<List<Product>>() {
            @Override
            public void onChanged(List<Product> products) {
                updateProductList(products);
            }
        });
    }

    private void updateProductList(List<Product> products) {
        if (products != null) {
            productList.clear();
            productList.addAll(products);
            adapter.updateData(products);
            updateProductCount();
            updateEmptyState();

            Log.d(TAG, "Updated product list with " + products.size() + " products (Category: " + currentCategory + ")");
        }
    }

    private void updateEmptyState() {
        if (productList.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            productRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            productRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void updateProductCount() {
        if (productCountText != null) {
            if (productList.isEmpty()) {
                productCountText.setText("No products in " + currentCategory);
            } else {
                productCountText.setText("Total: " + productList.size() + " product" +
                        (productList.size() == 1 ? "" : "s") + " (" + currentCategory + ")");
            }
            Log.d(TAG, "Updated product count: " + productList.size());
        }
    }

    private void openProductDetail(Product product) {
        Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("product_name", product.getName());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        intent.putExtra("expiry_date", sdf.format(product.getExpiryDate()));

        startActivityForResult(intent, 100);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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

    // Keep your existing theme methods
    private void applyThemeFromDatabase() {
        User user = userRepository.getUserSync();

        if (user != null) {
            String theme = user.getColorTheme();
            Log.d(TAG, "Applying theme from database: " + theme);
            ThemeUtils.applyTheme(this, theme);
            applyCustomThemeColors(theme);
        } else {
            String theme = preferences.getString("color_theme", "white");
            Log.d(TAG, "No user in DB, applying theme from SharedPreferences: " + theme);
            ThemeUtils.applyTheme(this, theme);
            applyCustomThemeColors(theme);
        }
    }

    private void applyCustomThemeColors(String theme) {
        // Keep your existing theme color application code
        // This method stays the same as before
        int primaryColor;
        int textColor;
        int backgroundColor;
        int fabBackgroundColor = 0;

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
            case "white":
            default:
                primaryColor = getResources().getColor(R.color.color_primary_white);
                textColor = getResources().getColor(R.color.color_text_white);
                backgroundColor = getResources().getColor(R.color.color_background_white);
                fabBackgroundColor = getResources().getColor(R.color.color_fab_white);
                break;
        }

        // Apply colors
        if (headerLayout != null) {
            headerLayout.setBackgroundColor(primaryColor);
        }
        if (titleTextView != null) {
            titleTextView.setTextColor(textColor);
        }
        if (addButton != null) {
            addButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(fabBackgroundColor));
        }
        if (rootView != null) {
            rootView.setBackgroundColor(backgroundColor);
        }

        Log.d(TAG, "Custom theme colors applied for theme: " + theme);
    }
}
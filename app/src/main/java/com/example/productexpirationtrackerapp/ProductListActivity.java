package com.example.productexpirationtrackerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductListActivity extends AppCompatActivity {

    private static final String TAG = "ProductListDebug";

    // UI Components
    private TextView titleTextView;
    private ImageView addButton;
    private TextView productCountText;
    private ListView productListView;
    private View rootView;
    private LinearLayout bottomNavigation;
    private LinearLayout headerLayout; // ADDED THIS LINE

    // Category filter buttons
    private Button categoryAllButton, categoryFoodButton, categoryMedicineButton, categoryDrinksButton, categoryOtherButton;

    // Bottom Navigation
    private LinearLayout navProfile, navProducts, navSettings;
    private ImageView navProfileIcon, navProductsIcon, navSettingsIcon;
    private TextView navProfileText, navProductsText, navSettingsText;

    // Data
    private ArrayList<Product> productList;
    private ProductListAdapter adapter;
    private SharedPreferences preferences;
    private UserRepository userRepository;
    private ProductViewModel productViewModel;

    // Current category tracking
    private String currentCategory = "All";

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

        // Setup product list (load from database)
        setupProductList();

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
        int fabBackgroundColor = 0; // ADD THIS

        // Get colors based on theme
        switch (theme) {
            case "green":
                primaryColor = getResources().getColor(R.color.color_primary_green);
                textColor = getResources().getColor(R.color.color_text_green);
                backgroundColor = getResources().getColor(R.color.color_background_green);
                fabBackgroundColor = getResources().getColor(R.color.color_fab_green); // ADD THIS
                break;
            case "blue":
                primaryColor = getResources().getColor(R.color.color_primary_blue);
                textColor = getResources().getColor(R.color.color_text_blue);
                backgroundColor = getResources().getColor(R.color.color_background_blue);
                fabBackgroundColor = getResources().getColor(R.color.color_fab_blue); // ADD THIS
                break;
            case "pink":
                primaryColor = getResources().getColor(R.color.color_primary_pink);
                textColor = getResources().getColor(R.color.color_text_pink);
                backgroundColor = getResources().getColor(R.color.color_background_pink);
                fabBackgroundColor = getResources().getColor(R.color.color_fab_pink); // ADD THIS
                break;
            case "purple":
                primaryColor = getResources().getColor(R.color.color_primary_purple);
                textColor = getResources().getColor(R.color.color_text_purple);
                backgroundColor = getResources().getColor(R.color.color_background_purple);
                fabBackgroundColor = getResources().getColor(R.color.color_fab_purple); // ADD THIS
                break;
            case "black":
                primaryColor = getResources().getColor(R.color.color_primary_black);
                textColor = getResources().getColor(R.color.color_text_black);
                backgroundColor = getResources().getColor(R.color.color_background_black);
                fabBackgroundColor = getResources().getColor(R.color.color_fab_black); // ADD THIS
                break;
            case "white":
            default:
                primaryColor = getResources().getColor(R.color.color_primary_white);
                textColor = getResources().getColor(R.color.color_text_white);
                backgroundColor = getResources().getColor(R.color.color_background_white);
                fabBackgroundColor = getResources().getColor(R.color.color_fab_white); // ADD THIS
                break;
        }

        // DEBUG LOGGING - ADD THIS
        Log.d(TAG, "Theme: " + theme);
        Log.d(TAG, "Background Color: " + String.format("#%08X", backgroundColor));
        Log.d(TAG, "FAB Background Color: " + String.format("#%08X", fabBackgroundColor));

        // Apply colors to views if they exist
        if (titleTextView != null) {
            titleTextView.setTextColor(textColor);
        }

        if (productCountText != null) {
            productCountText.setTextColor(textColor);
        }

        // Apply background to root view
        if (rootView != null) {
            rootView.setBackgroundColor(backgroundColor);
        }

        // Apply background to ListView
        if (productListView != null) {
            productListView.setBackgroundColor(backgroundColor);
        }

        // Apply header background color - SAME AS FAB
        if (headerLayout != null) {
            headerLayout.setBackgroundColor(fabBackgroundColor); // USE FAB COLOR
            Log.d(TAG, "Set header to FAB color: " + String.format("#%08X", fabBackgroundColor));
        }

        // Apply FAB and bottom navigation colors
        applyFABAndBottomNavColors();
    }

    private void applyFABAndBottomNavColors() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        int fabBackgroundColor = prefs.getInt("fab_background_color",
                getResources().getColor(R.color.color_fab_white));
        int fabIconColor = prefs.getInt("fab_icon_color",
                getResources().getColor(R.color.color_fab_icon_white));

        // Apply FAB colors
        if (addButton != null) {
            addButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(fabBackgroundColor));
            addButton.setImageTintList(android.content.res.ColorStateList.valueOf(fabIconColor));
        }

        // Apply bottom navigation background color - SAME AS FAB
        if (bottomNavigation != null) {
            bottomNavigation.setBackgroundColor(fabBackgroundColor);
        }

        // Apply header background color - SAME AS FAB
        if (headerLayout != null) {
            headerLayout.setBackgroundColor(fabBackgroundColor);
        }

        // Make all navigation icons/text WHITE for contrast
        int whiteColor = Color.WHITE;

        if (navProfileIcon != null) {
            navProfileIcon.setImageTintList(android.content.res.ColorStateList.valueOf(whiteColor));
        }
        if (navProfileText != null) {
            navProfileText.setTextColor(whiteColor);
        }
        if (navProductsIcon != null) {
            navProductsIcon.setImageTintList(android.content.res.ColorStateList.valueOf(whiteColor));
        }
        if (navProductsText != null) {
            navProductsText.setTextColor(whiteColor);
        }
        if (navSettingsIcon != null) {
            navSettingsIcon.setImageTintList(android.content.res.ColorStateList.valueOf(whiteColor));
        }
        if (navSettingsText != null) {
            navSettingsText.setTextColor(whiteColor);
        }
    }

    private void initializeViews() {
        Log.d(TAG, "Starting initializeViews");

        try {
            // Initialize views
            titleTextView = findViewById(R.id.titleTextView);
            addButton = findViewById(R.id.addButton);
            productCountText = findViewById(R.id.productCountText);
            productListView = findViewById(R.id.productListView);
            bottomNavigation = findViewById(R.id.bottomNavigation);
            headerLayout = findViewById(R.id.headerLayout); // ADDED THIS LINE

            Log.d(TAG, "Views found successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error in initializeViews: " + e.getMessage());
            Toast.makeText(this, "View error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initializeCategoryButtons() {
        categoryAllButton = findViewById(R.id.categoryAllButton);
        categoryFoodButton = findViewById(R.id.categoryFoodButton);
        categoryMedicineButton = findViewById(R.id.categoryMedicineButton);
        categoryDrinksButton = findViewById(R.id.categoryDrinksButton);
        categoryOtherButton = findViewById(R.id.categoryOtherButton);
    }

    private void initializeBottomNavigation() {
        // Initialize bottom navigation views
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

    private void setupProductList() {
        Log.d(TAG, "Setting up product list from database");

        // Initialize product list
        productList = new ArrayList<>();

        // Create custom adapter
        adapter = new ProductListAdapter(this, productList);

        // Set adapter to ListView
        if (productListView != null) {
            productListView.setAdapter(adapter);
            Log.d(TAG, "Adapter set to ListView");
        }

        // Set user's name in title
        String userName = preferences.getString("user_name", "User");
        if (titleTextView != null) {
            titleTextView.setText("📦 " + userName + "'s Products");
            Log.d(TAG, "Title set to: " + titleTextView.getText());
        }
    }

    private void setupClickListeners() {
        Log.d(TAG, "Setting up click listeners");

        // Add button (FAB) - open AddProductActivity
        if (addButton != null) {
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Add button clicked - opening AddProductActivity");
                    Intent intent = new Intent(ProductListActivity.this, AddProductActivity.class);
                    startActivityForResult(intent, 200);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        }

        // Category button click listeners
        if (categoryAllButton != null) {
            categoryAllButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentCategory = "All";
                    setActiveCategoryButton(categoryAllButton);
                    loadAllProducts();
                }
            });
        }

        if (categoryFoodButton != null) {
            categoryFoodButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentCategory = "Food";
                    setActiveCategoryButton(categoryFoodButton);
                    loadProductsByCategory("Food");
                }
            });
        }

        if (categoryMedicineButton != null) {
            categoryMedicineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentCategory = "Medicine";
                    setActiveCategoryButton(categoryMedicineButton);
                    loadProductsByCategory("Medicine");
                }
            });
        }

        if (categoryDrinksButton != null) {
            categoryDrinksButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentCategory = "Drinks";
                    setActiveCategoryButton(categoryDrinksButton);
                    loadProductsByCategory("Drinks");
                }
            });
        }

        if (categoryOtherButton != null) {
            categoryOtherButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentCategory = "Other";
                    setActiveCategoryButton(categoryOtherButton);
                    loadProductsByCategory("Other");
                }
            });
        }

        // Bottom Navigation Click Listeners
        if (navProfile != null) {
            navProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Profile navigation clicked");
                    // Navigate to MainActivity (Profile)
                    Intent intent = new Intent(ProductListActivity.this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                }
            });
        }

        if (navProducts != null) {
            navProducts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Products navigation clicked - already on this screen");
                    // Already on products screen - do nothing or scroll to top
                    if (productListView != null) {
                        productListView.smoothScrollToPosition(0);
                    }
                }
            });
        }

        if (navSettings != null) {
            navSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Settings navigation clicked");
                    // Navigate to SettingsActivity
                    Intent intent = new Intent(ProductListActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                }
            });
        }

        // List item click - OPEN PRODUCT DETAILS
        if (productListView != null) {
            productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position < productList.size()) {
                        Product product = productList.get(position);

                        Log.d(TAG, "Opening product details: " + product.getName());

                        // Open ProductDetailActivity
                        Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
                        intent.putExtra("product_id", product.getId());
                        intent.putExtra("product_name", product.getName());

                        // Convert date to string for passing
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        intent.putExtra("expiry_date", sdf.format(product.getExpiryDate()));

                        startActivityForResult(intent, 100);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                }
            });
        }

        // List item long click - delete product
        if (productListView != null) {
            productListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position < productList.size()) {
                        Product product = productList.get(position);

                        // Delete from database
                        productViewModel.delete(product);

                        Toast.makeText(ProductListActivity.this,
                                "Removed: " + product.getName(),
                                Toast.LENGTH_SHORT).show();

                        Log.d(TAG, "Item long clicked and removed from database: " + product.getName());
                    }
                    return true;
                }
            });
        }
    }

    private void setActiveCategoryButton(Button activeButton) {
        // Reset all buttons to semi-transparent white
        categoryAllButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0x80FFFFFF));
        categoryFoodButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0x80FFFFFF));
        categoryMedicineButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0x80FFFFFF));
        categoryDrinksButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0x80FFFFFF));
        categoryOtherButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0x80FFFFFF));

        // Set active button to solid white
        activeButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0xFFFFFFFF));
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

            Log.d(TAG, "Updated product list with " + products.size() + " products (Category: " + currentCategory + ")");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200 && resultCode == RESULT_OK) {
            // Product was added successfully from AddProductActivity
            Toast.makeText(this, "Product added!", Toast.LENGTH_SHORT).show();
            // The list will automatically update due to LiveData observation
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
            if (productList.isEmpty()) {
                productCountText.setText("No products in " + currentCategory);
            } else {
                productCountText.setText("Total: " + productList.size() + " product" +
                        (productList.size() == 1 ? "" : "s") + " (" + currentCategory + ")");
            }
            Log.d(TAG, "Updated product count: " + productList.size());
        }
    }
}
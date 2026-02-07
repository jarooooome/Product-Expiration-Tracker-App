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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductListActivity extends AppCompatActivity {

    private static final String TAG = "ProductListDebug";

    // UI Components
    private TextView titleTextView;
    private Button backButton, addButton;
    private TextView productCountText;
    private ListView productListView;
    private View rootView;

    // NEW: Category filter buttons
    private Button categoryAllButton, categoryFoodButton, categoryMedicineButton, categoryDrinksButton, categoryOtherButton;

    // Data
    private ArrayList<Product> productList;
    private ProductListAdapter adapter;
    private SharedPreferences preferences;
    private UserRepository userRepository;
    private ProductViewModel productViewModel;

    // NEW: Current category tracking
    private String currentCategory = "All"; // Default category

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "ProductListActivity onCreate started");

        try {
            setContentView(R.layout.activity_product_list);
            Log.d(TAG, "Simple layout set successfully");
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

        // NEW: Initialize category buttons
        initializeCategoryButtons();

        // Apply theme from database
        applyThemeFromDatabase();

        // Setup product list (load from database)
        setupProductList();

        // Setup button click listeners
        setupClickListeners();

        // NEW: Load all products by default
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

    private void loadProductsFromDatabase() {
        Log.d(TAG, "Loading products from database");

        productViewModel.getAllProducts().observe(this, new Observer<List<Product>>() {
            @Override
            public void onChanged(List<Product> products) {
                Log.d(TAG, "Products loaded from database: " + products.size());

                // Clear current list
                productList.clear();

                if (products == null || products.isEmpty()) {
                    Log.d(TAG, "No products in database");
                    updateProductCount();
                    return;
                }

                // Add all products to list
                productList.addAll(products);

                // Update adapter
                adapter.updateData(products);
                updateProductCount();

                Log.d(TAG, "Product list updated with " + productList.size() + " products");
            }
        });
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

        // Get colors based on theme
        switch (theme) {
            case "green":
                primaryColor = getResources().getColor(R.color.color_primary_green);
                textColor = getResources().getColor(R.color.color_text_green);
                backgroundColor = getResources().getColor(R.color.color_background_green);
                break;
            case "blue":
                primaryColor = getResources().getColor(R.color.color_primary_blue);
                textColor = getResources().getColor(R.color.color_text_blue);
                backgroundColor = getResources().getColor(R.color.color_background_blue);
                break;
            case "pink":
                primaryColor = getResources().getColor(R.color.color_primary_pink);
                textColor = getResources().getColor(R.color.color_text_pink);
                backgroundColor = getResources().getColor(R.color.color_background_pink);
                break;
            case "purple":
                primaryColor = getResources().getColor(R.color.color_primary_purple);
                textColor = getResources().getColor(R.color.color_text_purple);
                backgroundColor = getResources().getColor(R.color.color_background_purple);
                break;
            case "black":
                primaryColor = getResources().getColor(R.color.color_primary_black);
                textColor = getResources().getColor(R.color.color_text_black);
                backgroundColor = getResources().getColor(R.color.color_background_black);
                break;
            case "white":
            default:
                primaryColor = getResources().getColor(R.color.color_primary_white);
                textColor = getResources().getColor(R.color.color_text_white);
                backgroundColor = getResources().getColor(R.color.color_background_white);
                break;
        }

        // Apply colors to views if they exist
        if (titleTextView != null) {
            titleTextView.setTextColor(textColor);
        }

        if (productCountText != null) {
            productCountText.setTextColor(textColor);
        }

        // Apply button background colors
        if (backButton != null) {
            backButton.setBackgroundColor(primaryColor);
        }

        if (addButton != null) {
            addButton.setBackgroundColor(primaryColor);
        }

        // Apply background to root view
        if (rootView != null) {
            rootView.setBackgroundColor(backgroundColor);
        }

        // Apply background to ListView
        if (productListView != null) {
            productListView.setBackgroundColor(backgroundColor);
        }
    }

    private void initializeViews() {
        Log.d(TAG, "Starting initializeViews for simple layout");

        try {
            // Initialize views
            titleTextView = findViewById(R.id.titleTextView);
            backButton = findViewById(R.id.backButton);
            addButton = findViewById(R.id.addButton);
            productCountText = findViewById(R.id.productCountText);
            productListView = findViewById(R.id.productListView);

            Log.d(TAG, "Simple views found successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error in initializeViews: " + e.getMessage());
            Toast.makeText(this, "View error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // NEW: Initialize category buttons
    private void initializeCategoryButtons() {
        categoryAllButton = findViewById(R.id.categoryAllButton);
        categoryFoodButton = findViewById(R.id.categoryFoodButton);
        categoryMedicineButton = findViewById(R.id.categoryMedicineButton);
        categoryDrinksButton = findViewById(R.id.categoryDrinksButton);
        categoryOtherButton = findViewById(R.id.categoryOtherButton);
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

        // Back button - go to MainActivity
        if (backButton != null) {
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Back button clicked");
                    Intent intent = new Intent(ProductListActivity.this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                }
            });
        }

        // Add button - open AddProductActivity
        if (addButton != null) {
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Add button clicked - opening AddProductActivity");
                    Intent intent = new Intent(ProductListActivity.this, AddProductActivity.class);
                    startActivityForResult(intent, 200); // Use different request code
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        }

        // NEW: Category button click listeners
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

    // NEW: Set active category button
    private void setActiveCategoryButton(Button activeButton) {
        // Reset all buttons to gray
        categoryAllButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE0E0E0));
        categoryFoodButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE0E0E0));
        categoryMedicineButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE0E0E0));
        categoryDrinksButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE0E0E0));
        categoryOtherButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE0E0E0));

        // Set active button to blue
        activeButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2196F3));
    }

    // NEW: Load all products
    private void loadAllProducts() {
        productViewModel.getAllProducts().observe(this, new Observer<List<Product>>() {
            @Override
            public void onChanged(List<Product> products) {
                updateProductList(products);
            }
        });
    }

    // NEW: Load products by category
    private void loadProductsByCategory(String category) {
        productViewModel.getProductsByCategory(category).observe(this, new Observer<List<Product>>() {
            @Override
            public void onChanged(List<Product> products) {
                updateProductList(products);
            }
        });
    }

    // NEW: Update product list with data
    private void updateProductList(List<Product> products) {
        if (products != null) {
            productList.clear();
            productList.addAll(products);
            adapter.updateData(products);
            updateProductCount();

            Log.d(TAG, "Updated product list with " + products.size() + " products (Category: " + currentCategory + ")");
        }
    }

    // Handle result from ProductDetailActivity (for deletion) and AddProductActivity
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
                productCountText.setText("No products added yet");
            } else {
                productCountText.setText("Total: " + productList.size() + " product" +
                        (productList.size() == 1 ? "" : "s") + " (" + currentCategory + ")");
            }
            Log.d(TAG, "Updated product count: " + productList.size());
        }
    }
}
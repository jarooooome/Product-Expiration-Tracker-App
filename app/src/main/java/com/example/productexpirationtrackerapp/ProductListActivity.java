package com.example.productexpirationtrackerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import android.widget.ScrollView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductListActivity extends AppCompatActivity {

    private static final String TAG = "ProductListDebug";

    // UI Components
    private TextView hiUserTextView;
    private TextView welcomeDescriptionTextView;
    private TextView categoryLabelTextView;
    private ImageView addButton;
    private TextView productCountText;
    private WrapContentRecyclerView productRecyclerView;
    private View rootView;
    private LinearLayout bottomNavigation;
    private LinearLayout headerLayout;
    private EditText searchEditText;
    private TextView searchEmoji;
    private ImageView clearSearchButton;
    private HorizontalScrollView categoryScrollView;
    private ScrollView scrollView;

    // Category filter buttons - UPDATED with food-specific categories
    private LinearLayout categoryAllButton, categoryDairyButton, categoryVegetablesButton, categoryFruitsButton,
            categoryMeatsButton, categoryBeveragesButton, categoryMedicineButton, categoryOtherButton;

    // Category count TextViews - UPDATED with food-specific categories
    private TextView categoryAllCount, categoryDairyCount, categoryVegetablesCount, categoryFruitsCount,
            categoryMeatsCount, categoryBeveragesCount, categoryMedicineCount, categoryOtherCount;

    // Category title TextViews - UPDATED with food-specific categories
    private TextView categoryDairyTitle, categoryVegetablesTitle, categoryFruitsTitle,
            categoryMeatsTitle, categoryBeveragesTitle, categoryMedicineTitle, categoryOtherTitle;

    // Category icon TextViews - UPDATED with food-specific categories
    private TextView categoryDairyIcon, categoryVegetablesIcon, categoryFruitsIcon,
            categoryMeatsIcon, categoryBeveragesIcon, categoryMedicineIcon, categoryOtherIcon;

    // Bottom Navigation
    private LinearLayout navProfile, navProducts, navSettings;
    private ImageView navProfileIcon, navProductsIcon, navSettingsIcon;
    private TextView navProfileText, navProductsText, navSettingsText;

    // Data
    private ArrayList<Product> productList;
    private ArrayList<Product> allProducts;
    private ProductAdapter adapter;
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
            case "white":
            default:
                primaryColor = getResources().getColor(R.color.color_primary_white);
                textColor = getResources().getColor(R.color.color_text_white);
                backgroundColor = getResources().getColor(R.color.color_background_white);
                fabBackgroundColor = getResources().getColor(R.color.color_fab_white);
                break;
        }

        // DEBUG LOGGING
        Log.d(TAG, "Theme: " + theme);
        Log.d(TAG, "Background Color: " + String.format("#%08X", backgroundColor));
        Log.d(TAG, "FAB Color: " + String.format("#%08X", fabBackgroundColor));

        // Save FAB color to preferences for later use
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("fab_background_color", fabBackgroundColor);
        editor.apply();

        // Apply colors to views if they exist
        // Apply FAB color to big "Hi, User!" text
        if (hiUserTextView != null) {
            hiUserTextView.setTextColor(fabBackgroundColor);
        }

        // Apply FAB color to small description text
        if (welcomeDescriptionTextView != null) {
            welcomeDescriptionTextView.setTextColor(fabBackgroundColor);
        }

        // Apply FAB color to category label
        if (categoryLabelTextView != null) {
            categoryLabelTextView.setTextColor(fabBackgroundColor);
        }

        if (productCountText != null) {
            productCountText.setTextColor(textColor);
        }

        // Apply background to root view
        if (rootView != null) {
            rootView.setBackgroundColor(backgroundColor);
        }

        // Apply background to RecyclerView
        if (productRecyclerView != null) {
            productRecyclerView.setBackgroundColor(backgroundColor);
        }

        // Header background color matches body color
        if (headerLayout != null) {
            headerLayout.setBackgroundColor(backgroundColor);
            Log.d(TAG, "Set header to body color: " + String.format("#%08X", backgroundColor));
        }

        // Apply search bar styling
        if (searchEditText != null) {
            searchEditText.setTextColor(textColor);
            searchEditText.setHintTextColor(Color.parseColor("#999999"));
        }

        // Set category button colors to match FAB
        setCategoryButtonColors(fabBackgroundColor);

        // Set active category button (default to "All")
        if (categoryAllButton != null) {
            setActiveCategoryButton(categoryAllButton, fabBackgroundColor);
        }

        // Apply FAB and bottom navigation colors
        applyFABAndBottomNavColors();
    }

    private void setCategoryButtonColors(int fabColor) {
        // All buttons start with lighter FAB color
        float[] hsv = new float[3];
        Color.colorToHSV(fabColor, hsv);
        hsv[2] = 0.8f; // Reduce brightness to 80% for lighter version
        int lighterFabColor = Color.HSVToColor(hsv);

        if (categoryAllButton != null) {
            categoryAllButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(lighterFabColor));
        }
        if (categoryDairyButton != null) {
            categoryDairyButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(lighterFabColor));
        }
        if (categoryVegetablesButton != null) {
            categoryVegetablesButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(lighterFabColor));
        }
        if (categoryFruitsButton != null) {
            categoryFruitsButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(lighterFabColor));
        }
        if (categoryMeatsButton != null) {
            categoryMeatsButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(lighterFabColor));
        }
        if (categoryBeveragesButton != null) {
            categoryBeveragesButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(lighterFabColor));
        }
        if (categoryMedicineButton != null) {
            categoryMedicineButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(lighterFabColor));
        }
        if (categoryOtherButton != null) {
            categoryOtherButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(lighterFabColor));
        }
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

        // Search emoji - set to white for contrast on search bar
        if (searchEmoji != null) {
            searchEmoji.setTextColor(whiteColor);
        }
        if (clearSearchButton != null) {
            clearSearchButton.setImageTintList(android.content.res.ColorStateList.valueOf(whiteColor));
        }
    }

    private void initializeViews() {
        Log.d(TAG, "Starting initializeViews");

        try {
            // Initialize views
            hiUserTextView = findViewById(R.id.hiUserTextView);
            welcomeDescriptionTextView = findViewById(R.id.welcomeDescriptionTextView);
            categoryLabelTextView = findViewById(R.id.categoryLabelTextView);
            addButton = findViewById(R.id.addButton);
            productCountText = findViewById(R.id.productCountText);
            productRecyclerView = findViewById(R.id.productRecyclerView);
            bottomNavigation = findViewById(R.id.bottomNavigation);
            headerLayout = findViewById(R.id.headerLayout);
            searchEditText = findViewById(R.id.searchEditText);
            searchEmoji = findViewById(R.id.searchEmoji);
            clearSearchButton = findViewById(R.id.clearSearchButton);
            categoryScrollView = findViewById(R.id.categoryScrollView);
            scrollView = findViewById(R.id.scrollView);

            Log.d(TAG, "Views found successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error in initializeViews: " + e.getMessage());
            Toast.makeText(this, "View error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initializeCategoryButtons() {
        // Initialize category button containers (LinearLayouts)
        categoryAllButton = findViewById(R.id.categoryAllButton);
        categoryDairyButton = findViewById(R.id.categoryDairyButton);
        categoryVegetablesButton = findViewById(R.id.categoryVegetablesButton);
        categoryFruitsButton = findViewById(R.id.categoryFruitsButton);
        categoryMeatsButton = findViewById(R.id.categoryMeatsButton);
        categoryBeveragesButton = findViewById(R.id.categoryBeveragesButton);
        categoryMedicineButton = findViewById(R.id.categoryMedicineButton);
        categoryOtherButton = findViewById(R.id.categoryOtherButton);

        // Initialize count TextViews
        categoryAllCount = findViewById(R.id.categoryAllCount);
        categoryDairyCount = findViewById(R.id.categoryDairyCount);
        categoryVegetablesCount = findViewById(R.id.categoryVegetablesCount);
        categoryFruitsCount = findViewById(R.id.categoryFruitsCount);
        categoryMeatsCount = findViewById(R.id.categoryMeatsCount);
        categoryBeveragesCount = findViewById(R.id.categoryBeveragesCount);
        categoryMedicineCount = findViewById(R.id.categoryMedicineCount);
        categoryOtherCount = findViewById(R.id.categoryOtherCount);

        // Initialize title TextViews
        categoryDairyTitle = findViewById(R.id.categoryDairyTitle);
        categoryVegetablesTitle = findViewById(R.id.categoryVegetablesTitle);
        categoryFruitsTitle = findViewById(R.id.categoryFruitsTitle);
        categoryMeatsTitle = findViewById(R.id.categoryMeatsTitle);
        categoryBeveragesTitle = findViewById(R.id.categoryBeveragesTitle);
        categoryMedicineTitle = findViewById(R.id.categoryMedicineTitle);
        categoryOtherTitle = findViewById(R.id.categoryOtherTitle);

        // Initialize icon TextViews
        categoryDairyIcon = findViewById(R.id.categoryDairyIcon);
        categoryVegetablesIcon = findViewById(R.id.categoryVegetablesIcon);
        categoryFruitsIcon = findViewById(R.id.categoryFruitsIcon);
        categoryMeatsIcon = findViewById(R.id.categoryMeatsIcon);
        categoryBeveragesIcon = findViewById(R.id.categoryBeveragesIcon);
        categoryMedicineIcon = findViewById(R.id.categoryMedicineIcon);
        categoryOtherIcon = findViewById(R.id.categoryOtherIcon);

        Log.d(TAG, "Category buttons initialized with food-specific categories");
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

    private void setupSearchFunctionality() {
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // Not needed
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = s.toString();
                    filterProducts(currentSearchQuery);

                    // Show/hide clear button
                    if (clearSearchButton != null) {
                        clearSearchButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // Not needed
                }
            });
        }

        if (clearSearchButton != null) {
            clearSearchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchEditText.setText("");
                    currentSearchQuery = "";
                    clearSearchButton.setVisibility(View.GONE);
                }
            });
        }
    }

    private void filterProducts(String query) {
        if (allProducts == null || allProducts.isEmpty()) {
            return;
        }

        if (query == null || query.trim().isEmpty()) {
            // Show all products in current category
            updateProductList(allProducts);
            return;
        }

        String lowerCaseQuery = query.toLowerCase().trim();
        ArrayList<Product> filteredList = new ArrayList<>();

        for (Product product : allProducts) {
            String productName = product.getName();
            if (productName != null && productName.toLowerCase().contains(lowerCaseQuery)) {
                filteredList.add(product);
            } else {
                String category = product.getCategory();
                if (category != null && category.toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(product);
                }
            }
        }

        updateProductList(filteredList);

        Log.d(TAG, "Filtered " + filteredList.size() + " products from search: " + query);
    }

    private void setupProductList() {
        Log.d(TAG, "Setting up product list from database");

        // Initialize product list
        productList = new ArrayList<>();
        allProducts = new ArrayList<>();

        // Create RecyclerView adapter
        adapter = new ProductAdapter(productList);

        // Set up WrapContentRecyclerView
        if (productRecyclerView != null) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            productRecyclerView.setLayoutManager(layoutManager);
            productRecyclerView.setAdapter(adapter);
            productRecyclerView.setNestedScrollingEnabled(false);
            productRecyclerView.setHasFixedSize(false);

            Log.d(TAG, "Adapter set to WrapContentRecyclerView");
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
                    int fabColor = getFabColorFromPreferences();
                    setActiveCategoryButton(categoryAllButton, fabColor);
                    loadAllProducts();
                }
            });
        }

        if (categoryDairyButton != null) {
            categoryDairyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentCategory = "Dairy";
                    int fabColor = getFabColorFromPreferences();
                    setActiveCategoryButton(categoryDairyButton, fabColor);
                    loadProductsByCategory("Dairy");
                }
            });
        }

        if (categoryVegetablesButton != null) {
            categoryVegetablesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentCategory = "Vegetables";
                    int fabColor = getFabColorFromPreferences();
                    setActiveCategoryButton(categoryVegetablesButton, fabColor);
                    loadProductsByCategory("Vegetables");
                }
            });
        }

        if (categoryFruitsButton != null) {
            categoryFruitsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentCategory = "Fruits";
                    int fabColor = getFabColorFromPreferences();
                    setActiveCategoryButton(categoryFruitsButton, fabColor);
                    loadProductsByCategory("Fruits");
                }
            });
        }

        if (categoryMeatsButton != null) {
            categoryMeatsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentCategory = "Meats";
                    int fabColor = getFabColorFromPreferences();
                    setActiveCategoryButton(categoryMeatsButton, fabColor);
                    loadProductsByCategory("Meats");
                }
            });
        }

        if (categoryBeveragesButton != null) {
            categoryBeveragesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentCategory = "Beverages";
                    int fabColor = getFabColorFromPreferences();
                    setActiveCategoryButton(categoryBeveragesButton, fabColor);
                    loadProductsByCategory("Beverages");
                }
            });
        }

        if (categoryMedicineButton != null) {
            categoryMedicineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentCategory = "Medicine";
                    int fabColor = getFabColorFromPreferences();
                    setActiveCategoryButton(categoryMedicineButton, fabColor);
                    loadProductsByCategory("Medicine");
                }
            });
        }

        if (categoryOtherButton != null) {
            categoryOtherButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentCategory = "Other";
                    int fabColor = getFabColorFromPreferences();
                    setActiveCategoryButton(categoryOtherButton, fabColor);
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
                    if (scrollView != null) {
                        scrollView.smoothScrollTo(0, 0);
                    }
                }
            });
        }

        if (navSettings != null) {
            navSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Settings navigation clicked");
                    Intent intent = new Intent(ProductListActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                }
            });
        }

        // RecyclerView click listeners
        if (adapter != null) {
            adapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    Product product = productList.get(position);
                    Log.d(TAG, "Opening product details: " + product.getName());

                    Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
                    intent.putExtra("product_id", product.getId());
                    intent.putExtra("product_name", product.getName());

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    intent.putExtra("expiry_date", sdf.format(product.getExpiryDate()));

                    startActivityForResult(intent, 100);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }

                @Override
                public void onItemLongClick(int position) {
                    Product product = productList.get(position);
                    productViewModel.delete(product);

                    Toast.makeText(ProductListActivity.this,
                            "Removed: " + product.getName(),
                            Toast.LENGTH_SHORT).show();

                    Log.d(TAG, "Item long clicked and removed from database: " + product.getName());
                }
            });
        }
    }

    // UPDATED: to work with LinearLayout instead of TextView
    private void setActiveCategoryButton(LinearLayout activeButton, int fabColor) {
        // Reset all buttons to lighter FAB color
        float[] hsv = new float[3];
        Color.colorToHSV(fabColor, hsv);
        hsv[2] = 0.8f;
        int lighterFabColor = Color.HSVToColor(hsv);

        if (categoryAllButton != null) {
            categoryAllButton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(lighterFabColor));
        }
        if (categoryDairyButton != null) {
            categoryDairyButton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(lighterFabColor));
        }
        if (categoryVegetablesButton != null) {
            categoryVegetablesButton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(lighterFabColor));
        }
        if (categoryFruitsButton != null) {
            categoryFruitsButton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(lighterFabColor));
        }
        if (categoryMeatsButton != null) {
            categoryMeatsButton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(lighterFabColor));
        }
        if (categoryBeveragesButton != null) {
            categoryBeveragesButton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(lighterFabColor));
        }
        if (categoryMedicineButton != null) {
            categoryMedicineButton.setBackgroundTintList(
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
                    int dairyCount = 0, vegetablesCount = 0, fruitsCount = 0, meatsCount = 0;
                    int beveragesCount = 0, medicineCount = 0, otherCount = 0;

                    for (Product product : products) {
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
                    }

                    // Update ALL button count
                    if (categoryAllCount != null) {
                        categoryAllCount.setText(allCount + " items");
                    }

                    // Update Dairy button count
                    if (categoryDairyCount != null) {
                        categoryDairyCount.setText(dairyCount + " items");
                    }

                    // Update Vegetables button count
                    if (categoryVegetablesCount != null) {
                        categoryVegetablesCount.setText(vegetablesCount + " items");
                    }

                    // Update Fruits button count
                    if (categoryFruitsCount != null) {
                        categoryFruitsCount.setText(fruitsCount + " items");
                    }

                    // Update Meats button count
                    if (categoryMeatsCount != null) {
                        categoryMeatsCount.setText(meatsCount + " items");
                    }

                    // Update Beverages button count
                    if (categoryBeveragesCount != null) {
                        categoryBeveragesCount.setText(beveragesCount + " items");
                    }

                    // Update Medicine button count
                    if (categoryMedicineCount != null) {
                        categoryMedicineCount.setText(medicineCount + " items");
                    }

                    // Update Other button count
                    if (categoryOtherCount != null) {
                        categoryOtherCount.setText(otherCount + " items");
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
            adapter.updateData(productList);
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
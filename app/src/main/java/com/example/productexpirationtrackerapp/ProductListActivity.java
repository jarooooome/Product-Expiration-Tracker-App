package com.example.productexpirationtrackerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
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
import android.app.AlarmManager;
import android.content.Context;
import android.net.Uri;
import android.provider.Settings;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductListActivity extends AppCompatActivity {

    private static final String TAG = "ProductListDebug";
    private static final int NOTIFICATION_PERMISSION_CODE = 1001;
    private static final int VIBRATE_PERMISSION_CODE      = 1002;
    private static final int SCAN_FROM_LIST_REQUEST_CODE  = 300;

    // UI Components
    private TextView hiUserTextView;
    private ImageView historyButton;
    private android.view.View searchBarContainer;
    private ImageView listViewButton;
    private ImageView gridViewButton;
    private LinearLayout viewToggleContainer;
    private boolean isListView = true;
    private TextView welcomeDescriptionTextView;
    private TextView categoryLabelTextView;
    private ImageView addButton;
    private TextView productCountText;
    private WrapContentRecyclerView productRecyclerView;
    private View rootView;
    private LinearLayout bottomNavigation;
    private LinearLayout headerLayout;
    private EditText searchEditText;
    private ImageView searchEmoji;
    private ImageView clearSearchButton;
    private ImageView barcodeScannerButton;
    private HorizontalScrollView categoryScrollView;
    private ScrollView scrollView;

    // Bulk Action UI
    private LinearLayout bulkActionLayout;
    private TextView bulkActionTitle;
    private TextView bulkSelectAll;
    private TextView bulkDiscard;
    private TextView bulkCancel;

    // Bottom Navigation Indicators
    private View navProfileIndicator;
    private View navProductsIndicator;
    private View navSettingsIndicator;

    // Category filter buttons
    private LinearLayout categoryAllButton, categoryDairyButton, categoryVegetablesButton, categoryFruitsButton,
            categoryMeatsButton, categoryBeveragesButton, categoryMedicineButton, categoryOtherButton;

    // Category count TextViews
    private TextView categoryAllCount, categoryDairyCount, categoryVegetablesCount, categoryFruitsCount,
            categoryMeatsCount, categoryBeveragesCount, categoryMedicineCount, categoryOtherCount;

    // Category title TextViews
    private TextView categoryAllTitle, categoryDairyTitle, categoryVegetablesTitle, categoryFruitsTitle,
            categoryMeatsTitle, categoryBeveragesTitle, categoryMedicineTitle, categoryOtherTitle;

    // Category icon TextViews
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
    private ConsumedProductViewModel consumedProductViewModel;

    // Bulk selection
    private boolean isInBulkMode = false;
    private ArrayList<Integer> selectedPositions = new ArrayList<>();

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

        // Initialize ViewModels
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        consumedProductViewModel = new ViewModelProvider(this).get(ConsumedProductViewModel.class);

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

        // Request notification permission for Android 13+
        requestNotificationPermission();

        // Request vibrate permission for Android 13+
        requestVibratePermission();

        // Check exact alarm permission for Android 12+
        checkExactAlarmPermission();

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

        // Force refresh ng product list
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        // Exit bulk mode if active
        if (isInBulkMode) {
            exitBulkMode();
        }

        Log.d(TAG, "onResume called, theme reapplied and product list refreshed");
    }

    /**
     * Request notification permission for Android 13+
     */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    /**
     * Request vibrate permission for Android 13+
     */
    private void requestVibratePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.VIBRATE},
                        VIBRATE_PERMISSION_CODE);
            }
        }
    }

    /**
     * Check if app has exact alarm permission for Android 12+
     */
    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this,
                        "Please allow exact alarms for expiry notifications",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission granted");
                Toast.makeText(this, "You will receive expiry notifications", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "Notification permission denied");
                Toast.makeText(this,
                        "Enable notifications to get expiry reminders",
                        Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == VIBRATE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Vibrate permission granted");
            } else {
                Log.d(TAG, "Vibrate permission denied");
                Toast.makeText(this,
                        "Enable vibration to get haptic feedback for notifications",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private String getTimeBasedGreeting() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 12) {
            return "Good morning";
        } else if (hour >= 12 && hour < 17) {
            return "Good afternoon";
        } else {
            return "Good evening";
        }
    }

    private void updateWelcomeText() {
        Log.d(TAG, "updateWelcomeText() called");

        if (hiUserTextView == null) {
            Log.e(TAG, "hiUserTextView is null, cannot update welcome text");
            return;
        }

        // Show time-based placeholder immediately while DB loads
        hiUserTextView.setText(getTimeBasedGreeting() + ", ...");

        userRepository.getUser(new UserRepository.UserRepositoryCallback() {
            @Override
            public void onUserLoaded(User user) {
                String userName = "User";
                String source   = "default";

                if (user != null) {
                    String dbName = user.getUserName();
                    if (dbName != null && !dbName.trim().isEmpty()) {
                        userName = dbName.trim();
                        source   = "database";
                    } else {
                        String spName = preferences.getString("user_name", "").trim();
                        if (!spName.isEmpty()) { userName = spName; source = "SharedPreferences (db empty)"; }
                    }
                } else {
                    String spName = preferences.getString("user_name", "").trim();
                    if (!spName.isEmpty()) { userName = spName; source = "SharedPreferences (no db user)"; }
                }

                Log.d(TAG, "Greeting source: " + source + " -> " + userName);

                final String finalText = getTimeBasedGreeting() + ", " + userName + "!";
                runOnUiThread(() -> {
                    if (hiUserTextView != null) {
                        hiUserTextView.setText(finalText);
                        Log.d(TAG, "Greeting set: " + finalText);
                    }
                });
            }
        });
    }

    private void initializeViews() {
        Log.d(TAG, "Starting initializeViews");

        try {
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
            searchBarContainer = findViewById(R.id.searchBarContainer);
            clearSearchButton = findViewById(R.id.clearSearchButton);
            barcodeScannerButton = findViewById(R.id.barcodeScannerButton);
            if (barcodeScannerButton != null) {
                barcodeScannerButton.setOnClickListener(v -> {
                    Intent scanIntent = new Intent(ProductListActivity.this,
                            ProductScannerActivity.class);
                    startActivityForResult(scanIntent, SCAN_FROM_LIST_REQUEST_CODE);
                });
            }
            categoryScrollView = findViewById(R.id.categoryScrollView);
            scrollView = findViewById(R.id.scrollView);
            historyButton = findViewById(R.id.historyButton);
            listViewButton = findViewById(R.id.listViewButton);
            gridViewButton = findViewById(R.id.gridViewButton);
            viewToggleContainer = findViewById(R.id.viewToggleContainer);

            // Bulk Action Views
            bulkActionLayout = findViewById(R.id.bulkActionLayout);
            bulkActionTitle = findViewById(R.id.bulkActionTitle);
            bulkSelectAll = findViewById(R.id.bulkSelectAll);
            bulkDiscard = findViewById(R.id.bulkDiscard);
            bulkCancel = findViewById(R.id.bulkCancel);

            Log.d(TAG, "Views found successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error in initializeViews: " + e.getMessage());
            Toast.makeText(this, "View error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initializeCategoryButtons() {
        categoryAllButton = findViewById(R.id.categoryAllButton);
        categoryDairyButton = findViewById(R.id.categoryDairyButton);
        categoryVegetablesButton = findViewById(R.id.categoryVegetablesButton);
        categoryFruitsButton = findViewById(R.id.categoryFruitsButton);
        categoryMeatsButton = findViewById(R.id.categoryMeatsButton);
        categoryBeveragesButton = findViewById(R.id.categoryBeveragesButton);
        categoryMedicineButton = findViewById(R.id.categoryMedicineButton);
        categoryOtherButton = findViewById(R.id.categoryOtherButton);

        categoryAllCount = findViewById(R.id.categoryAllCount);
        categoryDairyCount = findViewById(R.id.categoryDairyCount);
        categoryVegetablesCount = findViewById(R.id.categoryVegetablesCount);
        categoryFruitsCount = findViewById(R.id.categoryFruitsCount);
        categoryMeatsCount = findViewById(R.id.categoryMeatsCount);
        categoryBeveragesCount = findViewById(R.id.categoryBeveragesCount);
        categoryMedicineCount = findViewById(R.id.categoryMedicineCount);
        categoryOtherCount = findViewById(R.id.categoryOtherCount);

        categoryAllTitle = findViewById(R.id.categoryAllTitle);
        categoryDairyTitle = findViewById(R.id.categoryDairyTitle);
        categoryVegetablesTitle = findViewById(R.id.categoryVegetablesTitle);
        categoryFruitsTitle = findViewById(R.id.categoryFruitsTitle);
        categoryMeatsTitle = findViewById(R.id.categoryMeatsTitle);
        categoryBeveragesTitle = findViewById(R.id.categoryBeveragesTitle);
        categoryMedicineTitle = findViewById(R.id.categoryMedicineTitle);
        categoryOtherTitle = findViewById(R.id.categoryOtherTitle);

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

        Log.d(TAG, "Bottom navigation initialized");
    }

    private void setupSearchFunctionality() {
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = s.toString();
                    filterProducts(currentSearchQuery);

                    if (clearSearchButton != null) {
                        clearSearchButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
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

        productList = new ArrayList<>();
        allProducts = new ArrayList<>();

        adapter = new ProductAdapter(productList);

        // Set up item click and long click listeners
        adapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (isInBulkMode) {
                    toggleSelection(position);
                }
                // Normal mode: do nothing — use the 3-dot Edit button to open product details
            }

            @Override
            public void onItemLongClick(int position) {
                // Enter bulk mode and select the item
                enterBulkMode();
                toggleSelection(position);
            }
        });

        adapter.setOnMenuClickListener(new ProductAdapter.OnMenuClickListener() {
            @Override
            public void onEditClick(int position) {
                Product product = productList.get(position);
                Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
                intent.putExtra("product_id", product.getId());
                intent.putExtra("product_name", product.getName());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                intent.putExtra("expiry_date", sdf.format(product.getExpiryDate()));
                startActivityForResult(intent, 100);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }

            @Override
            public void onDeleteClick(int position) {
                Product product = productList.get(position);
                new android.app.AlertDialog.Builder(ProductListActivity.this)
                        .setTitle("Delete Product")
                        .setMessage("Are you sure you want to delete \"" + product.getName() + "\"?")
                        .setPositiveButton("Delete", (dialog, which) -> productViewModel.delete(product))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        if (productRecyclerView != null) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            productRecyclerView.setLayoutManager(layoutManager);
            productRecyclerView.setAdapter(adapter);
            productRecyclerView.setNestedScrollingEnabled(false);
            productRecyclerView.setHasFixedSize(false);

            Log.d(TAG, "Adapter set to WrapContentRecyclerView");
        }
    }

    private void enterBulkMode() {
        isInBulkMode = true;
        selectedPositions.clear();

        // Show bulk action bar
        if (bulkActionLayout != null) {
            bulkActionLayout.setVisibility(View.VISIBLE);
        }

        // Hide other UI elements
        if (categoryScrollView != null) categoryScrollView.setVisibility(View.GONE);
        if (addButton != null) addButton.setVisibility(View.GONE);
        if (searchEditText != null) searchEditText.setVisibility(View.GONE);
        if (searchEmoji != null) searchEmoji.setVisibility(View.GONE);
        if (historyButton != null) historyButton.setVisibility(View.GONE);

        updateBulkActionTitle();
    }

    private void exitBulkMode() {
        isInBulkMode = false;
        selectedPositions.clear();

        // Hide bulk action bar
        if (bulkActionLayout != null) {
            bulkActionLayout.setVisibility(View.GONE);
        }

        // Show other UI elements
        if (categoryScrollView != null) categoryScrollView.setVisibility(View.VISIBLE);
        if (addButton != null) addButton.setVisibility(View.VISIBLE);
        if (searchEditText != null) searchEditText.setVisibility(View.VISIBLE);
        if (searchEmoji != null) searchEmoji.setVisibility(View.VISIBLE);
        if (historyButton != null) historyButton.setVisibility(View.VISIBLE);

        // Refresh adapter to remove selection highlights
        if (adapter != null) {
            adapter.setSelectedPositions(null);
            adapter.notifyDataSetChanged();
        }
    }

    private void toggleSelection(int position) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(Integer.valueOf(position));
        } else {
            selectedPositions.add(position);
        }

        // Update adapter to show selection
        adapter.setSelectedPositions(selectedPositions);
        adapter.notifyItemChanged(position);

        updateBulkActionTitle();
    }

    private void updateBulkActionTitle() {
        if (bulkActionTitle != null) {
            bulkActionTitle.setText(selectedPositions.size() + " item(s) selected");
        }
    }

    private void selectAll() {
        selectedPositions.clear();
        for (int i = 0; i < productList.size(); i++) {
            selectedPositions.add(i);
        }

        adapter.setSelectedPositions(selectedPositions);
        adapter.notifyDataSetChanged();

        updateBulkActionTitle();
    }

    private void discardSelected() {
        if (selectedPositions.isEmpty()) {
            Toast.makeText(this, "No items selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Discard Items")
                .setMessage("Are you sure you want to discard " + selectedPositions.size() + " item(s)?")
                .setPositiveButton("DISCARD", (dialog, which) -> {
                    // Save to history and delete each selected product
                    for (int position : selectedPositions) {
                        Product product = productList.get(position);

                        // Save to history as DISCARDED
                        ConsumedProduct consumedProduct = new ConsumedProduct(
                                product.getId(),
                                product.getName(),
                                product.getCategory(),
                                product.getQuantity(),
                                product.getExpiryDate(),
                                "DISCARDED",
                                product.getPhoto()
                        );
                        consumedProductViewModel.insert(consumedProduct);

                        // Delete from products table
                        productViewModel.delete(product);
                    }

                    Toast.makeText(ProductListActivity.this,
                            selectedPositions.size() + " item(s) discarded",
                            Toast.LENGTH_SHORT).show();

                    exitBulkMode();
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void setupClickListeners() {
        Log.d(TAG, "Setting up click listeners");

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

        if (listViewButton != null) {
            listViewButton.setOnClickListener(v -> {
                if (!isListView) {
                    isListView = true;
                    listViewButton.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4361EE")));
                    gridViewButton.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#999999")));
                    if (productRecyclerView != null)
                        productRecyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(ProductListActivity.this));
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
                    if (productRecyclerView != null)
                        productRecyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(ProductListActivity.this));
                    adapter.setGridView(true);
                }
            });
        }

        if (historyButton != null) {
            historyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "History button clicked - opening ConsumedHistoryActivity");
                    Intent intent = new Intent(ProductListActivity.this, ConsumedHistoryActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        }

        // Bulk action listeners
        if (bulkSelectAll != null) {
            bulkSelectAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectAll();
                }
            });
        }

        if (bulkDiscard != null) {
            bulkDiscard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    discardSelected();
                }
            });
        }

        if (bulkCancel != null) {
            bulkCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    exitBulkMode();
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
                    Log.d(TAG, "Profile navigation clicked - opening ProfileActivity");
                    Intent intent = new Intent(ProductListActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
                }
            });
        }
    }

    // Theme methods - SIMPLIFIED for White and Black only
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
        int primaryColor;
        int textColor;
        int backgroundColor;
        int fabBackgroundColor;
        int fabIconColor;

        // Only White and Black themes remain
        if ("black".equals(theme)) {
            // Black/Dark Theme - SOLID BLACK
            primaryColor = Color.parseColor("#121212"); // Solid black background
            textColor = Color.WHITE; // White text
            backgroundColor = Color.parseColor("#121212"); // Solid black background
            fabBackgroundColor = Color.parseColor("#1E1E1E"); // Slightly lighter black for FAB
            fabIconColor = Color.WHITE; // White icon for FAB

            // FIXED: ALL categories now use white text in dark theme (matching Other)
            if (categoryAllTitle != null) {
                categoryAllTitle.setTextColor(Color.WHITE);
                categoryAllTitle.setBackgroundColor(Color.TRANSPARENT);
            }
            if (categoryAllCount != null) {
                categoryAllCount.setTextColor(Color.WHITE);
                categoryAllCount.setBackgroundColor(Color.TRANSPARENT);
            }
            if (categoryDairyIcon != null) {
                categoryDairyIcon.setTextColor(Color.WHITE);
                categoryDairyIcon.setBackgroundColor(Color.TRANSPARENT);
            }
            if (categoryDairyTitle != null) {
                categoryDairyTitle.setTextColor(Color.WHITE);
                categoryDairyTitle.setBackgroundColor(Color.TRANSPARENT);
            }
            if (categoryDairyCount != null) {
                categoryDairyCount.setTextColor(Color.WHITE);
                categoryDairyCount.setBackgroundColor(Color.TRANSPARENT);
            }
            if (categoryVegetablesIcon != null) {
                categoryVegetablesIcon.setTextColor(Color.WHITE);
                categoryVegetablesIcon.setBackgroundColor(Color.TRANSPARENT);
            }
            if (categoryVegetablesTitle != null) {
                categoryVegetablesTitle.setTextColor(Color.WHITE);
                categoryVegetablesTitle.setBackgroundColor(Color.TRANSPARENT);
            }
            if (categoryVegetablesCount != null) {
                categoryVegetablesCount.setTextColor(Color.WHITE);
                categoryVegetablesCount.setBackgroundColor(Color.TRANSPARENT);
            }
            if (categoryFruitsIcon != null) {
                categoryFruitsIcon.setTextColor(Color.WHITE);
                categoryFruitsIcon.setBackgroundColor(Color.TRANSPARENT);
            }
            if (categoryFruitsTitle != null) {
                categoryFruitsTitle.setTextColor(Color.WHITE);
                categoryFruitsTitle.setBackgroundColor(Color.TRANSPARENT);
            }
            if (categoryFruitsCount != null) {
                categoryFruitsCount.setTextColor(Color.WHITE);
                categoryFruitsCount.setBackgroundColor(Color.TRANSPARENT);
            }
            if (categoryMeatsIcon != null) {
                categoryMeatsIcon.setTextColor(Color.WHITE);
                categoryMeatsIcon.setBackgroundColor(Color.TRANSPARENT);
            }
            if (categoryMeatsTitle != null) {
                categoryMeatsTitle.setTextColor(Color.WHITE);
                categoryMeatsTitle.setBackgroundColor(Color.TRANSPARENT);
            }
            if (categoryMeatsCount != null) {
                categoryMeatsCount.setTextColor(Color.WHITE);
                categoryMeatsCount.setBackgroundColor(Color.TRANSPARENT);
            }
            if (categoryBeveragesIcon != null) {
                categoryBeveragesIcon.setTextColor(Color.WHITE);
                categoryBeveragesIcon.setBackgroundColor(Color.TRANSPARENT);
            }
            if (categoryBeveragesTitle != null) {
                categoryBeveragesTitle.setTextColor(Color.WHITE);
                categoryBeveragesTitle.setBackgroundColor(Color.TRANSPARENT);
            }
            if (categoryBeveragesCount != null) {
                categoryBeveragesCount.setTextColor(Color.WHITE);
                categoryBeveragesCount.setBackgroundColor(Color.TRANSPARENT);
            }
            if (categoryMedicineIcon != null) {
                categoryMedicineIcon.setTextColor(Color.WHITE);
                categoryMedicineIcon.setBackgroundColor(Color.TRANSPARENT);
            }
            if (categoryMedicineTitle != null) {
                categoryMedicineTitle.setTextColor(Color.WHITE);
                categoryMedicineTitle.setBackgroundColor(Color.TRANSPARENT);
            }
            if (categoryMedicineCount != null) {
                categoryMedicineCount.setTextColor(Color.WHITE);
                categoryMedicineCount.setBackgroundColor(Color.TRANSPARENT);
            }
            // Other category - already white, but ensure consistency
            if (categoryOtherIcon != null) {
                categoryOtherIcon.setTextColor(Color.WHITE);
                categoryOtherIcon.setBackgroundColor(Color.TRANSPARENT);
            }
            if (categoryOtherTitle != null) {
                categoryOtherTitle.setTextColor(Color.WHITE);
                categoryOtherTitle.setBackgroundColor(Color.TRANSPARENT);
            }
            if (categoryOtherCount != null) {
                categoryOtherCount.setTextColor(Color.WHITE);
                categoryOtherCount.setBackgroundColor(Color.TRANSPARENT);
            }

            // Set header text to white
            if (hiUserTextView != null) {
                hiUserTextView.setTextColor(Color.WHITE);
                hiUserTextView.setBackgroundColor(Color.TRANSPARENT);
            }
            if (welcomeDescriptionTextView != null) {
                welcomeDescriptionTextView.setTextColor(Color.LTGRAY);
                welcomeDescriptionTextView.setBackgroundColor(Color.TRANSPARENT);
            }
            if (categoryLabelTextView != null) {
                categoryLabelTextView.setTextColor(Color.WHITE);
                categoryLabelTextView.setBackgroundColor(Color.TRANSPARENT);
            }
            if (productCountText != null) {
                productCountText.setTextColor(Color.WHITE);
            }
            // Dark search bar container
            if (searchBarContainer != null) {
                android.graphics.drawable.GradientDrawable searchBgDark = new android.graphics.drawable.GradientDrawable();
                searchBgDark.setColor(Color.parseColor("#2C2C2C"));
                searchBgDark.setCornerRadius(50 * getResources().getDisplayMetrics().density);
                searchBarContainer.setBackground(searchBgDark);
            }
            if (searchEditText != null) {
                searchEditText.setTextColor(Color.WHITE);
                searchEditText.setHintTextColor(Color.parseColor("#888888"));
                searchEditText.setBackgroundColor(Color.TRANSPARENT);
            }
            if (searchEmoji != null) {
                searchEmoji.setColorFilter(Color.WHITE);
            }
            if (barcodeScannerButton != null) {
                barcodeScannerButton.setColorFilter(Color.WHITE);
                // Glass circle background in dark mode
                android.graphics.drawable.GradientDrawable glassCircle = new android.graphics.drawable.GradientDrawable();
                glassCircle.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                glassCircle.setColor(Color.argb(50, 255, 255, 255));
                glassCircle.setStroke(2, Color.argb(120, 255, 255, 255));
                barcodeScannerButton.setBackground(glassCircle);
            }
            if (historyButton != null) {
                historyButton.setColorFilter(Color.WHITE);
            }
            if (listViewButton != null)
                listViewButton.setImageTintList(android.content.res.ColorStateList.valueOf(
                        isListView ? Color.parseColor("#4361EE") : Color.parseColor("#AAAAAA")));
            if (gridViewButton != null)
                gridViewButton.setImageTintList(android.content.res.ColorStateList.valueOf(
                        isListView ? Color.parseColor("#AAAAAA") : Color.parseColor("#4361EE")));
            if (viewToggleContainer != null) {
                float dp = getResources().getDisplayMetrics().density;
                android.graphics.drawable.GradientDrawable toggleBg = new android.graphics.drawable.GradientDrawable();
                toggleBg.setColor(Color.parseColor("#2A2A2A"));
                toggleBg.setCornerRadius(32 * dp);
                toggleBg.setStroke(1, Color.parseColor("#444444"));
                viewToggleContainer.setBackground(toggleBg);
            }

            // Make category button backgrounds transparent
            if (categoryAllButton != null) {
                categoryAllButton.setBackgroundColor(Color.TRANSPARENT);
            }
            if (categoryDairyButton != null) {
                categoryDairyButton.setBackgroundColor(Color.TRANSPARENT);
            }
            if (categoryVegetablesButton != null) {
                categoryVegetablesButton.setBackgroundColor(Color.TRANSPARENT);
            }
            if (categoryFruitsButton != null) {
                categoryFruitsButton.setBackgroundColor(Color.TRANSPARENT);
            }
            if (categoryMeatsButton != null) {
                categoryMeatsButton.setBackgroundColor(Color.TRANSPARENT);
            }
            if (categoryBeveragesButton != null) {
                categoryBeveragesButton.setBackgroundColor(Color.TRANSPARENT);
            }
            if (categoryMedicineButton != null) {
                categoryMedicineButton.setBackgroundColor(Color.TRANSPARENT);
            }
            if (categoryOtherButton != null) {
                categoryOtherButton.setBackgroundColor(Color.TRANSPARENT);
            }

        } else {
            // White/Light Theme (Default) - SOLID BLACK TEXT
            primaryColor = Color.parseColor("#6200EE");
            textColor = Color.BLACK; // Solid black text
            backgroundColor = Color.parseColor("#F5F5F5"); // Light gray background
            fabBackgroundColor = Color.parseColor("#6200EE");
            fabIconColor = Color.WHITE;

            // Set header text to black
            if (hiUserTextView != null) {
                hiUserTextView.setTextColor(Color.BLACK);
            }
            if (welcomeDescriptionTextView != null) {
                welcomeDescriptionTextView.setTextColor(Color.DKGRAY);
            }
            if (categoryLabelTextView != null) {
                categoryLabelTextView.setTextColor(Color.BLACK);
            }
            if (productCountText != null) {
                productCountText.setTextColor(Color.BLACK);
            }
            // Light search bar container
            if (searchBarContainer != null) {
                android.graphics.drawable.GradientDrawable searchBgLight = new android.graphics.drawable.GradientDrawable();
                searchBgLight.setColor(Color.WHITE);
                searchBgLight.setCornerRadius(50 * getResources().getDisplayMetrics().density);
                searchBgLight.setStroke(1, Color.parseColor("#E0E0E0"));
                searchBarContainer.setBackground(searchBgLight);
            }
            if (searchEditText != null) {
                searchEditText.setTextColor(Color.BLACK);
                searchEditText.setHintTextColor(Color.parseColor("#80000000"));
            }
            if (searchEmoji != null) {
                searchEmoji.setColorFilter(Color.DKGRAY);
            }
            if (historyButton != null) {
                historyButton.setColorFilter(Color.BLACK);
            }
            if (listViewButton != null)
                listViewButton.setImageTintList(android.content.res.ColorStateList.valueOf(
                        isListView ? Color.parseColor("#4361EE") : Color.parseColor("#777777")));
            if (gridViewButton != null)
                gridViewButton.setImageTintList(android.content.res.ColorStateList.valueOf(
                        isListView ? Color.parseColor("#777777") : Color.parseColor("#4361EE")));
            if (viewToggleContainer != null) {
                float dp = getResources().getDisplayMetrics().density;
                android.graphics.drawable.GradientDrawable toggleBg = new android.graphics.drawable.GradientDrawable();
                toggleBg.setColor(Color.WHITE);
                toggleBg.setCornerRadius(32 * dp);
                toggleBg.setStroke(1, Color.parseColor("#DDDDDD"));
                viewToggleContainer.setBackground(toggleBg);
            }

            // Set category text to white (readable on colored backgrounds)
            if (categoryAllTitle != null) categoryAllTitle.setTextColor(Color.BLACK);
            if (categoryAllCount != null) categoryAllCount.setTextColor(Color.DKGRAY);
            if (categoryDairyIcon != null) categoryDairyIcon.setTextColor(Color.WHITE);
            if (categoryDairyTitle != null) categoryDairyTitle.setTextColor(Color.WHITE);
            if (categoryDairyCount != null) categoryDairyCount.setTextColor(Color.parseColor("#DDDDDD"));
            if (categoryVegetablesIcon != null) categoryVegetablesIcon.setTextColor(Color.WHITE);
            if (categoryVegetablesTitle != null) categoryVegetablesTitle.setTextColor(Color.WHITE);
            if (categoryVegetablesCount != null) categoryVegetablesCount.setTextColor(Color.parseColor("#DDDDDD"));
            if (categoryFruitsIcon != null) categoryFruitsIcon.setTextColor(Color.WHITE);
            if (categoryFruitsTitle != null) categoryFruitsTitle.setTextColor(Color.WHITE);
            if (categoryFruitsCount != null) categoryFruitsCount.setTextColor(Color.parseColor("#DDDDDD"));
            if (categoryMeatsIcon != null) categoryMeatsIcon.setTextColor(Color.WHITE);
            if (categoryMeatsTitle != null) categoryMeatsTitle.setTextColor(Color.WHITE);
            if (categoryMeatsCount != null) categoryMeatsCount.setTextColor(Color.parseColor("#DDDDDD"));
            if (categoryBeveragesIcon != null) categoryBeveragesIcon.setTextColor(Color.WHITE);
            if (categoryBeveragesTitle != null) categoryBeveragesTitle.setTextColor(Color.WHITE);
            if (categoryBeveragesCount != null) categoryBeveragesCount.setTextColor(Color.parseColor("#DDDDDD"));
            if (categoryMedicineIcon != null) categoryMedicineIcon.setTextColor(Color.WHITE);
            if (categoryMedicineTitle != null) categoryMedicineTitle.setTextColor(Color.WHITE);
            if (categoryMedicineCount != null) categoryMedicineCount.setTextColor(Color.parseColor("#DDDDDD"));
            if (categoryOtherIcon != null) categoryOtherIcon.setTextColor(Color.WHITE);
            if (categoryOtherTitle != null) categoryOtherTitle.setTextColor(Color.WHITE);
            if (categoryOtherCount != null) categoryOtherCount.setTextColor(Color.parseColor("#DDDDDD"));
        }

        Log.d(TAG, "Theme: " + theme);
        Log.d(TAG, "Background Color: " + String.format("#%08X", backgroundColor));
        Log.d(TAG, "FAB Color: " + String.format("#%08X", fabBackgroundColor));
        Log.d(TAG, "FAB Icon Color: " + String.format("#%08X", fabIconColor));

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("fab_background_color", fabBackgroundColor);
        editor.putInt("fab_icon_color", fabIconColor);
        editor.apply();

        if (productCountText != null) productCountText.setTextColor(textColor);
        if (rootView != null) rootView.setBackgroundColor(backgroundColor);
        if (productRecyclerView != null) productRecyclerView.setBackgroundColor(backgroundColor);
        if (headerLayout != null) headerLayout.setBackgroundColor(backgroundColor);

        // Always initialize category buttons for both themes
        if (categoryAllButton != null) {
            setActiveCategoryButton(categoryAllButton, fabBackgroundColor);
        }
        applyFABAndBottomNavColors();
    }

    private void setCategoryButtonColors(int fabColor) {
        float[] hsv = new float[3];
        Color.colorToHSV(fabColor, hsv);

        boolean isDarkTheme = (fabColor == getResources().getColor(R.color.color_fab_black));

        if (isDarkTheme) {
            // For black theme, make backgrounds transparent
            if (categoryAllButton != null) {
                categoryAllButton.setBackgroundColor(Color.TRANSPARENT);
                categoryAllButton.setBackgroundTintList(null);
            }
            if (categoryDairyButton != null) {
                categoryDairyButton.setBackgroundColor(Color.TRANSPARENT);
                categoryDairyButton.setBackgroundTintList(null);
            }
            if (categoryVegetablesButton != null) {
                categoryVegetablesButton.setBackgroundColor(Color.TRANSPARENT);
                categoryVegetablesButton.setBackgroundTintList(null);
            }
            if (categoryFruitsButton != null) {
                categoryFruitsButton.setBackgroundColor(Color.TRANSPARENT);
                categoryFruitsButton.setBackgroundTintList(null);
            }
            if (categoryMeatsButton != null) {
                categoryMeatsButton.setBackgroundColor(Color.TRANSPARENT);
                categoryMeatsButton.setBackgroundTintList(null);
            }
            if (categoryBeveragesButton != null) {
                categoryBeveragesButton.setBackgroundColor(Color.TRANSPARENT);
                categoryBeveragesButton.setBackgroundTintList(null);
            }
            if (categoryMedicineButton != null) {
                categoryMedicineButton.setBackgroundColor(Color.TRANSPARENT);
                categoryMedicineButton.setBackgroundTintList(null);
            }
            if (categoryOtherButton != null) {
                categoryOtherButton.setBackgroundColor(Color.TRANSPARENT);
                categoryOtherButton.setBackgroundTintList(null);
            }
        } else {
            hsv[2] = 0.8f;
            int lighterFabColor = Color.HSVToColor(hsv);
            if (categoryAllButton != null) categoryAllButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(lighterFabColor));
            if (categoryDairyButton != null) categoryDairyButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(lighterFabColor));
            if (categoryVegetablesButton != null) categoryVegetablesButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(lighterFabColor));
            if (categoryFruitsButton != null) categoryFruitsButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(lighterFabColor));
            if (categoryMeatsButton != null) categoryMeatsButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(lighterFabColor));
            if (categoryBeveragesButton != null) categoryBeveragesButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(lighterFabColor));
            if (categoryMedicineButton != null) categoryMedicineButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(lighterFabColor));
            if (categoryOtherButton != null) categoryOtherButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(lighterFabColor));
        }
    }

    private android.graphics.drawable.GradientDrawable makeRoundedBg(int color, boolean withStroke) {
        android.graphics.drawable.GradientDrawable d = new android.graphics.drawable.GradientDrawable();
        d.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        d.setCornerRadius(16f * getResources().getDisplayMetrics().density);
        d.setColor(color);
        if (withStroke) {
            d.setStroke(2, Color.argb(120, 255, 255, 255));
        }
        return d;
    }

    private void setActiveCategoryButton(LinearLayout activeButton, int fabColor) {
        User user = userRepository.getUserSync();
        boolean isBlackTheme = user != null && "black".equals(user.getColorTheme());

        // Per-category profile light colors (matches Profile activity)
        int[] categoryColors = {
                0, // All — handled separately
                ContextCompat.getColor(this, R.color.category_dairy_dark),
                ContextCompat.getColor(this, R.color.category_vegetables_dark),
                ContextCompat.getColor(this, R.color.category_fruits_dark),
                ContextCompat.getColor(this, R.color.category_meats_dark),
                ContextCompat.getColor(this, R.color.category_beverages_dark),
                ContextCompat.getColor(this, R.color.category_medicine_dark),
                ContextCompat.getColor(this, R.color.category_other_dark)
        };
        LinearLayout[] allButtons = {
                categoryAllButton, categoryDairyButton, categoryVegetablesButton,
                categoryFruitsButton, categoryMeatsButton, categoryBeveragesButton,
                categoryMedicineButton, categoryOtherButton
        };

        if (isBlackTheme) {
            // "All" button: glass effect
            if (categoryAllButton != null) {
                boolean isActive = categoryAllButton == activeButton;
                categoryAllButton.setBackground(makeRoundedBg(
                        isActive ? Color.argb(70, 255, 255, 255) : Color.argb(25, 255, 255, 255), isActive));
            }
            // Other buttons: profile light colors, with white border glow when active
            for (int i = 1; i < allButtons.length; i++) {
                LinearLayout btn = allButtons[i];
                if (btn != null) {
                    android.graphics.drawable.GradientDrawable d = makeRoundedBg(categoryColors[i], true);
                    if (btn == activeButton) {
                        // Brighter border when active
                        d.setStroke(3, Color.WHITE);
                    }
                    btn.setBackground(d);
                }
            }
            // FIXED: ALL categories now use white text in dark theme
            if (categoryAllTitle != null) categoryAllTitle.setTextColor(Color.WHITE);
            if (categoryAllCount  != null) categoryAllCount.setTextColor(Color.WHITE);
            if (categoryDairyTitle != null) categoryDairyTitle.setTextColor(Color.WHITE);
            if (categoryDairyCount != null) categoryDairyCount.setTextColor(Color.WHITE);
            if (categoryVegetablesTitle != null) categoryVegetablesTitle.setTextColor(Color.WHITE);
            if (categoryVegetablesCount != null) categoryVegetablesCount.setTextColor(Color.WHITE);
            if (categoryFruitsTitle != null) categoryFruitsTitle.setTextColor(Color.WHITE);
            if (categoryFruitsCount != null) categoryFruitsCount.setTextColor(Color.WHITE);
            if (categoryMeatsTitle != null) categoryMeatsTitle.setTextColor(Color.WHITE);
            if (categoryMeatsCount != null) categoryMeatsCount.setTextColor(Color.WHITE);
            if (categoryBeveragesTitle != null) categoryBeveragesTitle.setTextColor(Color.WHITE);
            if (categoryBeveragesCount != null) categoryBeveragesCount.setTextColor(Color.WHITE);
            if (categoryMedicineTitle != null) categoryMedicineTitle.setTextColor(Color.WHITE);
            if (categoryMedicineCount != null) categoryMedicineCount.setTextColor(Color.WHITE);
            if (categoryOtherTitle != null) categoryOtherTitle.setTextColor(Color.WHITE);
            if (categoryOtherCount != null) categoryOtherCount.setTextColor(Color.WHITE);

        } else {
            // Light mode: "All" grey, other buttons use profile light colors
            if (categoryAllButton != null) {
                boolean isActive = categoryAllButton == activeButton;
                categoryAllButton.setBackground(makeRoundedBg(
                        isActive ? Color.argb(60, 180, 180, 180) : Color.parseColor("#CCCCCC"), isActive));
            }
            for (int i = 1; i < allButtons.length; i++) {
                LinearLayout btn = allButtons[i];
                if (btn != null) {
                    if (btn == activeButton) {
                        android.graphics.drawable.GradientDrawable activeBg = makeRoundedBg(categoryColors[i], false);
                        activeBg.setStroke(3, Color.WHITE);
                        btn.setBackground(activeBg);
                    } else {
                        btn.setBackground(makeRoundedBg(categoryColors[i], false));
                    }
                }
            }
            // Active button text white (on blue), inactive text black
            // Exception: Other always white (dark navy background)
            // All category buttons have colored backgrounds — always white text
            if (categoryAllTitle != null) categoryAllTitle.setTextColor(categoryAllButton == activeButton ? Color.WHITE : Color.BLACK);
            if (categoryAllCount  != null) categoryAllCount.setTextColor(categoryAllButton == activeButton ? Color.WHITE : Color.DKGRAY);
            LinearLayout[] textBtns = {categoryDairyButton, categoryVegetablesButton, categoryFruitsButton,
                    categoryMeatsButton, categoryBeveragesButton, categoryMedicineButton, categoryOtherButton};
            TextView[][] textViews = {
                    {categoryDairyIcon, categoryDairyTitle, categoryDairyCount},
                    {categoryVegetablesIcon, categoryVegetablesTitle, categoryVegetablesCount},
                    {categoryFruitsIcon, categoryFruitsTitle, categoryFruitsCount},
                    {categoryMeatsIcon, categoryMeatsTitle, categoryMeatsCount},
                    {categoryBeveragesIcon, categoryBeveragesTitle, categoryBeveragesCount},
                    {categoryMedicineIcon, categoryMedicineTitle, categoryMedicineCount},
                    {categoryOtherIcon, categoryOtherTitle, categoryOtherCount}
            };
            for (int j = 0; j < textBtns.length; j++) {
                for (TextView tv : textViews[j]) { if (tv != null) tv.setTextColor(Color.WHITE); }
            }
        }

        // Scrollbar thumb color: white in dark mode, grey in light mode
        if (categoryScrollView != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            categoryScrollView.setHorizontalScrollbarThumbDrawable(
                    new android.graphics.drawable.ColorDrawable(
                            isBlackTheme ? Color.WHITE : Color.parseColor("#888888")));
        }
    }

    private void setCategoryTextColor(int color) {
        if (categoryAllTitle != null) categoryAllTitle.setTextColor(color);
        if (categoryAllCount != null) categoryAllCount.setTextColor(color);
        if (categoryDairyIcon != null) categoryDairyIcon.setTextColor(color);
        if (categoryDairyTitle != null) categoryDairyTitle.setTextColor(color);
        if (categoryDairyCount != null) categoryDairyCount.setTextColor(color);
        if (categoryVegetablesIcon != null) categoryVegetablesIcon.setTextColor(color);
        if (categoryVegetablesTitle != null) categoryVegetablesTitle.setTextColor(color);
        if (categoryVegetablesCount != null) categoryVegetablesCount.setTextColor(color);
        if (categoryFruitsIcon != null) categoryFruitsIcon.setTextColor(color);
        if (categoryFruitsTitle != null) categoryFruitsTitle.setTextColor(color);
        if (categoryFruitsCount != null) categoryFruitsCount.setTextColor(color);
        if (categoryMeatsIcon != null) categoryMeatsIcon.setTextColor(color);
        if (categoryMeatsTitle != null) categoryMeatsTitle.setTextColor(color);
        if (categoryMeatsCount != null) categoryMeatsCount.setTextColor(color);
        if (categoryBeveragesIcon != null) categoryBeveragesIcon.setTextColor(color);
        if (categoryBeveragesTitle != null) categoryBeveragesTitle.setTextColor(color);
        if (categoryBeveragesCount != null) categoryBeveragesCount.setTextColor(color);
        if (categoryMedicineIcon != null) categoryMedicineIcon.setTextColor(color);
        if (categoryMedicineTitle != null) categoryMedicineTitle.setTextColor(color);
        if (categoryMedicineCount != null) categoryMedicineCount.setTextColor(color);
        if (categoryOtherIcon != null) categoryOtherIcon.setTextColor(color);
        if (categoryOtherTitle != null) categoryOtherTitle.setTextColor(color);
        if (categoryOtherCount != null) categoryOtherCount.setTextColor(color);
    }

    // UPDATED: Fixed navigation bar colors for white theme
    private void applyFABAndBottomNavColors() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        int fabBackgroundColor = prefs.getInt("fab_background_color",
                getResources().getColor(R.color.color_fab_white));
        int fabIconColor = prefs.getInt("fab_icon_color",
                getResources().getColor(R.color.color_fab_icon_white));

        // Get current theme - check both DB and SharedPreferences as fallback
        User user = userRepository.getUserSync();
        String savedTheme = prefs.getString("color_theme", "white");
        boolean isBlackTheme = (user != null && "black".equals(user.getColorTheme())) || "black".equals(savedTheme);

        if (addButton != null) {
            if (isBlackTheme) {
                // Black theme: FAB background slightly lighter black, icon white
                addButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1E1E1E")));
                addButton.setImageTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
            } else {
                // White theme: FAB background purple, icon white
                addButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(fabBackgroundColor));
                addButton.setImageTintList(android.content.res.ColorStateList.valueOf(fabIconColor));
            }
        }

        if (bottomNavigation != null) {
            if (isBlackTheme) {
                // Rounded top corners nav background matching Settings (#1A1A1A)
                android.graphics.drawable.GradientDrawable navBgDark = new android.graphics.drawable.GradientDrawable();
                navBgDark.setColor(Color.parseColor("#1A1A1A"));
                float[] radii = {48f, 48f, 48f, 48f, 0f, 0f, 0f, 0f}; // top corners only
                navBgDark.setCornerRadii(radii);
                bottomNavigation.setBackground(navBgDark);

                // For black theme, icons and text should be white
                if (navProfileIcon != null) navProfileIcon.setColorFilter(Color.WHITE);
                if (navProfileText != null) navProfileText.setTextColor(Color.WHITE);
                if (navProductsIcon != null) navProductsIcon.setColorFilter(Color.WHITE);
                if (navProductsText != null) navProductsText.setTextColor(Color.WHITE);
                if (navSettingsIcon != null) navSettingsIcon.setColorFilter(Color.WHITE);
                if (navSettingsText != null) navSettingsText.setTextColor(Color.WHITE);

                if (navProfileIndicator != null) navProfileIndicator.setBackgroundColor(Color.WHITE);
                if (navProductsIndicator != null) navProductsIndicator.setBackgroundColor(Color.WHITE);
                if (navSettingsIndicator != null) navSettingsIndicator.setBackgroundColor(Color.WHITE);

                if (searchEmoji != null) searchEmoji.setColorFilter(Color.WHITE);
                if (clearSearchButton != null) clearSearchButton.setColorFilter(Color.WHITE);

                // OPTION 2: History icon - WHITE in dark mode
                if (historyButton != null) {
                    historyButton.setColorFilter(Color.WHITE);
                }

            } else {
                // Rounded top corners nav background (light mode)
                android.graphics.drawable.GradientDrawable navBgLight = new android.graphics.drawable.GradientDrawable();
                navBgLight.setColor(Color.parseColor("#1A1A1A"));
                float[] radiiL = {48f, 48f, 48f, 48f, 0f, 0f, 0f, 0f}; // top corners only
                navBgLight.setCornerRadii(radiiL);
                bottomNavigation.setBackground(navBgLight);
                bottomNavigation.setElevation(8f);

                if (navProfileIcon != null) navProfileIcon.setColorFilter(Color.WHITE);
                if (navProfileText != null) navProfileText.setTextColor(Color.WHITE);
                if (navProductsIcon != null) navProductsIcon.setColorFilter(Color.WHITE);
                if (navProductsText != null) navProductsText.setTextColor(Color.WHITE);
                if (navSettingsIcon != null) navSettingsIcon.setColorFilter(Color.WHITE);
                if (navSettingsText != null) navSettingsText.setTextColor(Color.WHITE);

                if (navProfileIndicator != null) navProfileIndicator.setBackgroundColor(Color.WHITE);
                if (navProductsIndicator != null) navProductsIndicator.setBackgroundColor(Color.WHITE);
                if (navSettingsIndicator != null) navSettingsIndicator.setBackgroundColor(Color.WHITE);

                if (searchEmoji != null) searchEmoji.setColorFilter(Color.DKGRAY);
                if (barcodeScannerButton != null) {
                    barcodeScannerButton.setColorFilter(Color.DKGRAY);
                    android.graphics.drawable.GradientDrawable greyCircle = new android.graphics.drawable.GradientDrawable();
                    greyCircle.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                    greyCircle.setColor(Color.parseColor("#E0E0E0"));
                    barcodeScannerButton.setBackground(greyCircle);
                }
                if (clearSearchButton != null) clearSearchButton.setColorFilter(Color.BLACK);

                // OPTION 2: History icon - BLACK in light mode
                if (historyButton != null) {
                    historyButton.setColorFilter(Color.BLACK);
                }
            }
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
                                case "Dairy": dairyCount++; break;
                                case "Vegetables": vegetablesCount++; break;
                                case "Fruits": fruitsCount++; break;
                                case "Meats": meatsCount++; break;
                                case "Beverages": beveragesCount++; break;
                                case "Medicine": medicineCount++; break;
                                case "Other": otherCount++; break;
                            }
                        }
                    }

                    if (categoryAllCount != null) categoryAllCount.setText(allCount + " items");
                    if (categoryDairyCount != null) categoryDairyCount.setText(dairyCount + " items");
                    if (categoryVegetablesCount != null) categoryVegetablesCount.setText(vegetablesCount + " items");
                    if (categoryFruitsCount != null) categoryFruitsCount.setText(fruitsCount + " items");
                    if (categoryMeatsCount != null) categoryMeatsCount.setText(meatsCount + " items");
                    if (categoryBeveragesCount != null) categoryBeveragesCount.setText(beveragesCount + " items");
                    if (categoryMedicineCount != null) categoryMedicineCount.setText(medicineCount + " items");
                    if (categoryOtherCount != null) categoryOtherCount.setText(otherCount + " items");
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

                    productViewModel.rescheduleAllNotifications();

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

        // Handle scan result from the shortcut barcode button
        if (requestCode == SCAN_FROM_LIST_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // Forward all scanned extras directly to AddProductActivity
            Intent addIntent = new Intent(ProductListActivity.this, AddProductActivity.class);
            if (data.hasExtra("barcode"))      addIntent.putExtra("barcode",      data.getStringExtra("barcode"));
            if (data.hasExtra("product_name")) addIntent.putExtra("product_name", data.getStringExtra("product_name"));
            if (data.hasExtra("category"))     addIntent.putExtra("category",     data.getStringExtra("category"));
            if (data.hasExtra("expiry_date"))  addIntent.putExtra("expiry_date",  data.getStringExtra("expiry_date"));
            if (data.hasExtra("batch_number")) addIntent.putExtra("batch_number", data.getStringExtra("batch_number"));
            startActivityForResult(addIntent, 200);
        }
    }

    private void updateProductCount() {
        if (productCountText != null) {
            String searchInfo = "";
            if (currentSearchQuery != null && !currentSearchQuery.isEmpty()) {
                searchInfo = " (searching: \"" + currentSearchQuery + "\")";
            }

            if (productList.isEmpty()) {
                productCountText.setText("No Products found" + searchInfo);
            } else {
                productCountText.setText(productList.size() + " product" +
                        (productList.size() == 1 ? "" : "s") +
                        " in " + currentCategory + searchInfo);
            }
            Log.d(TAG, "Updated product count: " + productList.size());
        }
    }
}
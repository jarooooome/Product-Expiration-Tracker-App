package com.example.productexpirationtrackerapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Enhanced Product List Activity with modern features:
 * - Real-time search
 * - Advanced filtering (category, expiry status)
 * - Pull-to-refresh
 * - Grid/List view toggle
 * - Statistics dashboard
 * - Swipe to delete
 * - Sort options
 * - Empty state
 * - Smooth animations
 */
public class ProductListActivity extends AppCompatActivity {

    // UI Components
    private EditText searchEditText;
    private ImageButton searchClearButton;
    private ImageButton sortButton;
    private ImageButton viewToggleButton;
    private ChipGroup filterChipGroup;
    private Chip chipAll, chipExpired, chipExpiringSoon, chipFresh;
    private RecyclerView productsRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabAdd;
    private BottomNavigationView bottomNavigation;

    // Statistics Cards
    private CardView statsCard;
    private TextView totalProductsText;
    private TextView expiredCountText;
    private TextView expiringSoonCountText;
    private TextView freshCountText;

    // Empty State
    private LinearLayout emptyStateLayout;
    private TextView emptyStateTitle;
    private TextView emptyStateMessage;
    private Button emptyStateActionButton;

    // Data & Adapter
    private AppDatabase database;
    private ProductGridAdapter productAdapter;
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> filteredProducts = new ArrayList<>();

    // Filter State
    private String currentCategory = "All";
    private String currentExpiryFilter = "All";
    private String currentSearchQuery = "";
    private String currentSortOption = "Expiry Date (Soonest)";
    private boolean isGridView = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        initializeViews();
        setupDatabase();
        setupRecyclerView();
        setupSearchBar();
        setupFilterChips();
        setupButtons();
        setupSwipeRefresh();
        setupBottomNavigation();

        loadProducts();
    }

    private void initializeViews() {
        // Search Bar
        searchEditText = findViewById(R.id.searchEditText);
        searchClearButton = findViewById(R.id.searchClearButton);
        sortButton = findViewById(R.id.sortButton);
        viewToggleButton = findViewById(R.id.viewToggleButton);

        // Filter Chips
        filterChipGroup = findViewById(R.id.filterChipGroup);
        chipAll = findViewById(R.id.chipAll);
        chipExpired = findViewById(R.id.chipExpired);
        chipExpiringSoon = findViewById(R.id.chipExpiringSoon);
        chipFresh = findViewById(R.id.chipFresh);

        // RecyclerView & Refresh
        productsRecyclerView = findViewById(R.id.productsRecyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Statistics
        statsCard = findViewById(R.id.statsCard);
        totalProductsText = findViewById(R.id.totalProductsText);
        expiredCountText = findViewById(R.id.expiredCountText);
        expiringSoonCountText = findViewById(R.id.expiringSoonCountText);
        freshCountText = findViewById(R.id.freshCountText);

        // Empty State
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        emptyStateTitle = findViewById(R.id.emptyStateTitle);
        emptyStateMessage = findViewById(R.id.emptyStateMessage);
        emptyStateActionButton = findViewById(R.id.emptyStateActionButton);

        // FAB & Navigation
        fabAdd = findViewById(R.id.fabAdd);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupDatabase() {
        database = AppDatabase.getDatabase(this);
    }

    private void setupRecyclerView() {
        // Grid Layout with 2 columns
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        productsRecyclerView.setLayoutManager(layoutManager);

        // Initialize adapter
        productAdapter = new ProductGridAdapter(this, filteredProducts, this::openProductDetail);
        productsRecyclerView.setAdapter(productAdapter);

        // Setup swipe to delete
        setupSwipeToDelete();
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;

                Product product = filteredProducts.get(position);

                // Show confirmation dialog
                new AlertDialog.Builder(ProductListActivity.this)
                        .setTitle(getString(R.string.delete_product_title))
                        .setMessage(getString(R.string.delete_product_message, product.getName()))
                        .setPositiveButton(getString(R.string.delete), (dialog, which) -> deleteProduct(product, position))
                        .setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                            // Restore the item
                            productAdapter.notifyItemChanged(position);
                        })
                        .setOnCancelListener(dialog -> {
                            // Restore the item if dialog is dismissed
                            productAdapter.notifyItemChanged(position);
                        })
                        .show();
            }
        });

        itemTouchHelper.attachToRecyclerView(productsRecyclerView);
    }

    private void setupSearchBar() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                searchClearButton.setVisibility(currentSearchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        searchClearButton.setOnClickListener(v -> {
            searchEditText.setText("");
            currentSearchQuery = "";
            applyFilters();
        });
    }

    private void setupFilterChips() {
        chipAll.setOnClickListener(v -> {
            currentExpiryFilter = "All";
            updateChipSelection(chipAll);
            applyFilters();
        });

        chipExpired.setOnClickListener(v -> {
            currentExpiryFilter = "Expired";
            updateChipSelection(chipExpired);
            applyFilters();
        });

        chipExpiringSoon.setOnClickListener(v -> {
            currentExpiryFilter = "Expiring Soon";
            updateChipSelection(chipExpiringSoon);
            applyFilters();
        });

        chipFresh.setOnClickListener(v -> {
            currentExpiryFilter = "Fresh";
            updateChipSelection(chipFresh);
            applyFilters();
        });
    }

    private void updateChipSelection(Chip selectedChip) {
        chipAll.setChecked(false);
        chipExpired.setChecked(false);
        chipExpiringSoon.setChecked(false);
        chipFresh.setChecked(false);
        selectedChip.setChecked(true);
    }

    private void setupButtons() {
        // Sort Button
        sortButton.setOnClickListener(v -> showSortMenu());

        // View Toggle Button
        viewToggleButton.setOnClickListener(v -> toggleViewMode());

        // FAB Add Button
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(ProductListActivity.this, AddProductActivity.class);
            startActivity(intent);
        });

        // Empty State Action Button
        emptyStateActionButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProductListActivity.this, AddProductActivity.class);
            startActivity(intent);
        });
    }

    private void showSortMenu() {
        PopupMenu popupMenu = new PopupMenu(this, sortButton);
        popupMenu.inflate(R.menu.sort_menu);

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.sort_expiry_soonest) {
                currentSortOption = "Expiry Date (Soonest)";
                sortProducts();
                return true;
            } else if (itemId == R.id.sort_expiry_latest) {
                currentSortOption = "Expiry Date (Latest)";
                sortProducts();
                return true;
            } else if (itemId == R.id.sort_name_az) {
                currentSortOption = "Name (A-Z)";
                sortProducts();
                return true;
            } else if (itemId == R.id.sort_name_za) {
                currentSortOption = "Name (Z-A)";
                sortProducts();
                return true;
            } else if (itemId == R.id.sort_quantity_high) {
                currentSortOption = "Quantity (High to Low)";
                sortProducts();
                return true;
            } else if (itemId == R.id.sort_quantity_low) {
                currentSortOption = "Quantity (Low to High)";
                sortProducts();
                return true;
            }

            return false;
        });

        popupMenu.show();
    }

    private void toggleViewMode() {
        isGridView = !isGridView;

        GridLayoutManager layoutManager = (GridLayoutManager) productsRecyclerView.getLayoutManager();
        if (layoutManager != null) {
            layoutManager.setSpanCount(isGridView ? 2 : 1);
        }

        // Update icon - using built-in icons since custom ones might not exist
        viewToggleButton.setImageResource(
                isGridView ? android.R.drawable.ic_menu_view : android.R.drawable.ic_menu_agenda
        );

        Toast.makeText(this,
                isGridView ? getString(R.string.grid_view) : getString(R.string.list_view),
                Toast.LENGTH_SHORT).show();
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_green_light,
                android.R.color.holo_blue_light,
                android.R.color.holo_orange_light
        );

        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadProducts();

            // Stop refreshing after 1.5 seconds
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                swipeRefreshLayout.setRefreshing(false);
            }, 1500);
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_inventory);

        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(ProductListActivity.this, HomeActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_inventory) {
                    return true;
                } else if (itemId == R.id.nav_settings) {
                    startActivity(new Intent(ProductListActivity.this, SettingsActivity.class));
                    finish();
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
                    applyFilters();
                    updateStatistics();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, getString(R.string.error_loading_products, e.getMessage()),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void applyFilters() {
        filteredProducts.clear();

        for (Product product : allProducts) {
            // Apply search filter
            if (!currentSearchQuery.isEmpty()) {
                String query = currentSearchQuery.toLowerCase();
                String name = product.getName().toLowerCase();
                String category = product.getCategory() != null ? product.getCategory().toLowerCase() : "";

                if (!name.contains(query) && !category.contains(query)) {
                    continue;
                }
            }

            // Apply expiry status filter
            if (!currentExpiryFilter.equals("All")) {
                long daysUntilExpiry = calculateDaysUntilExpiry(product.getExpiryDate());

                if (currentExpiryFilter.equals("Expired") && daysUntilExpiry >= 0) {
                    continue;
                } else if (currentExpiryFilter.equals("Expiring Soon") && (daysUntilExpiry < 0 || daysUntilExpiry > 7)) {
                    continue;
                } else if (currentExpiryFilter.equals("Fresh") && daysUntilExpiry <= 7) {
                    continue;
                }
            }

            filteredProducts.add(product);
        }

        sortProducts();
        updateEmptyState();
    }

    private void sortProducts() {
        switch (currentSortOption) {
            case "Expiry Date (Soonest)":
                filteredProducts.sort((p1, p2) -> {
                    if (p1.getExpiryDate() == null) return 1;
                    if (p2.getExpiryDate() == null) return -1;
                    return p1.getExpiryDate().compareTo(p2.getExpiryDate());
                });
                break;

            case "Expiry Date (Latest)":
                filteredProducts.sort((p1, p2) -> {
                    if (p1.getExpiryDate() == null) return 1;
                    if (p2.getExpiryDate() == null) return -1;
                    return p2.getExpiryDate().compareTo(p1.getExpiryDate());
                });
                break;

            case "Name (A-Z)":
                filteredProducts.sort((p1, p2) ->
                        p1.getName().compareToIgnoreCase(p2.getName()));
                break;

            case "Name (Z-A)":
                filteredProducts.sort((p1, p2) ->
                        p2.getName().compareToIgnoreCase(p1.getName()));
                break;

            case "Quantity (High to Low)":
                filteredProducts.sort((p1, p2) ->
                        Integer.compare(p2.getQuantity(), p1.getQuantity()));
                break;

            case "Quantity (Low to High)":
                filteredProducts.sort((p1, p2) ->
                        Integer.compare(p1.getQuantity(), p2.getQuantity()));
                break;
        }

        productAdapter.notifyDataSetChanged();
    }

    private void updateStatistics() {
        int total = allProducts.size();
        int expired = 0;
        int expiringSoon = 0;
        int fresh = 0;

        for (Product product : allProducts) {
            long daysUntilExpiry = calculateDaysUntilExpiry(product.getExpiryDate());

            if (daysUntilExpiry < 0) {
                expired++;
            } else if (daysUntilExpiry <= 7) {
                expiringSoon++;
            } else {
                fresh++;
            }
        }

        totalProductsText.setText(String.valueOf(total));
        expiredCountText.setText(String.valueOf(expired));
        expiringSoonCountText.setText(String.valueOf(expiringSoon));
        freshCountText.setText(String.valueOf(fresh));

        // Update chip badges
        chipExpired.setText(getString(R.string.filter_expired_count, expired));
        chipExpiringSoon.setText(getString(R.string.filter_expiring_soon_count, expiringSoon));
        chipFresh.setText(getString(R.string.filter_fresh_count, fresh));
    }

    private void updateEmptyState() {
        if (filteredProducts.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            productsRecyclerView.setVisibility(View.GONE);
            statsCard.setVisibility(View.GONE);

            if (allProducts.isEmpty()) {
                // No products at all
                emptyStateTitle.setText(getString(R.string.no_products_title));
                emptyStateMessage.setText(getString(R.string.no_products_message));
                emptyStateActionButton.setVisibility(View.VISIBLE);
            } else {
                // Products exist but filtered out
                emptyStateTitle.setText(getString(R.string.no_results_title));
                emptyStateMessage.setText(getString(R.string.no_results_message));
                emptyStateActionButton.setVisibility(View.GONE);
            }
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            productsRecyclerView.setVisibility(View.VISIBLE);
            statsCard.setVisibility(View.VISIBLE);
        }
    }

    private void deleteProduct(Product product, int position) {
        new Thread(() -> {
            try {
                database.productDao().delete(product);

                runOnUiThread(() -> {
                    // Remove from both lists
                    allProducts.remove(product);
                    filteredProducts.remove(position);

                    productAdapter.notifyItemRemoved(position);
                    updateStatistics();
                    updateEmptyState();

                    Toast.makeText(this, getString(R.string.product_deleted, product.getName()),
                            Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, getString(R.string.error_deleting_product, e.getMessage()),
                            Toast.LENGTH_SHORT).show();
                    productAdapter.notifyItemChanged(position);
                });
            }
        }).start();
    }

    private void openProductDetail(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("PRODUCT_ID", product.getId());
        startActivity(intent);
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

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }
}
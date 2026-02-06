package com.example.productexpirationtrackerapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView productsRecyclerView;
    private ProductGridAdapter productAdapter;
    private List<Product> productList;
    private List<Product> filteredList;
    private EditText searchEditText;
    private CardView addNewButton;
    private FloatingActionButton fab;
    private BottomNavigationView bottomNavigation;
    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeViews();
        setupDatabase();
        setupRecyclerView();
        setupSearch();
        setupButtons();
        setupBottomNavigation();
        loadProducts();
    }

    private void initializeViews() {
        productsRecyclerView = findViewById(R.id.productsRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        addNewButton = findViewById(R.id.addNewButton);
        fab = findViewById(R.id.fab);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupDatabase() {
        database = AppDatabase.getDatabase(this);
    }

    private void setupRecyclerView() {
        productList = new ArrayList<>();
        filteredList = new ArrayList<>();

        // Grid layout with 2 columns
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        productsRecyclerView.setLayoutManager(gridLayoutManager);

        productAdapter = new ProductGridAdapter(this, filteredList, new ProductGridAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                openProductDetail(product);
            }
        });

        productsRecyclerView.setAdapter(productAdapter);
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterProducts(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            filteredList.addAll(productList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Product product : productList) {
                if (product.getName().toLowerCase().contains(lowerCaseQuery) ||
                        (product.getCategory() != null && product.getCategory().toLowerCase().contains(lowerCaseQuery))) {
                    filteredList.add(product);
                }
            }
        }

        productAdapter.notifyDataSetChanged();
    }

    private void setupButtons() {
        addNewButton.setOnClickListener(v -> openAddProduct());
        fab.setOnClickListener(v -> openAddProduct());
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
                productList = database.productDao().getAllProducts();

                runOnUiThread(() -> {
                    filteredList.clear();
                    filteredList.addAll(productList);
                    productAdapter.notifyDataSetChanged();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(HomeActivity.this, "Error loading products: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void openAddProduct() {
        Intent intent = new Intent(this, AddProductActivity.class);
        startActivity(intent);
    }

    private void openProductDetail(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("PRODUCT_ID", product.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts(); // Reload products when returning to this activity
    }
}
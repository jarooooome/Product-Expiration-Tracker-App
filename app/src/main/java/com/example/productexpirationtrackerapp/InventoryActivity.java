package com.example.productexpirationtrackerapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class InventoryActivity extends AppCompatActivity {

    private RecyclerView inventoryRecyclerView;
    private ProductGridAdapter productAdapter;
    private List<Product> productList;
    private BottomNavigationView bottomNavigation;
    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // Reusing home layout for now

        initializeViews();
        setupDatabase();
        setupRecyclerView();
        setupBottomNavigation();
        loadProducts();
    }

    private void initializeViews() {
        inventoryRecyclerView = findViewById(R.id.productsRecyclerView);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupDatabase() {
        database = AppDatabase.getDatabase(this);
    }

    private void setupRecyclerView() {
        productList = new ArrayList<>();

        // Linear layout for list view
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        inventoryRecyclerView.setLayoutManager(layoutManager);

        productAdapter = new ProductGridAdapter(this, productList, product -> openProductDetail(product));

        inventoryRecyclerView.setAdapter(productAdapter);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_inventory);

        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(InventoryActivity.this, HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_inventory) {
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(InventoryActivity.this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            return false;
        });
    }

    private void loadProducts() {
        new Thread(() -> {
            try {
                productList = database.productDao().getAllProducts();

                runOnUiThread(() -> {
                    productAdapter.notifyDataSetChanged();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, getString(R.string.error_loading_products, e.getMessage()),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void openProductDetail(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("PRODUCT_ID", product.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }
}
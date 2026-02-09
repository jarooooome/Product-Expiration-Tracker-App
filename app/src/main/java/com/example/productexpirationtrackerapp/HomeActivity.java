package com.example.productexpirationtrackerapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Show that we successfully loaded
        Toast.makeText(this, "HomeActivity loaded successfully!", Toast.LENGTH_SHORT).show();

        // Initialize only essential components
        initializeBasicViews();

        // Don't load products yet - just test if activity loads
    }

    private void initializeBasicViews() {
        try {
            // Just find a few views to test
            FloatingActionButton fab = findViewById(R.id.fab);
            if (fab != null) {
                fab.setOnClickListener(v -> {
                    Toast.makeText(this, "Add Product clicked", Toast.LENGTH_SHORT).show();
                });
            }

            BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
            if (bottomNavigation != null) {
                bottomNavigation.setSelectedItemId(R.id.nav_home);
                bottomNavigation.setOnNavigationItemSelectedListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_home) {
                        return true;
                    } else if (itemId == R.id.nav_inventory) {
                        startActivity(new Intent(this, InventoryActivity.class));
                        return true;
                    } else if (itemId == R.id.nav_settings) {
                        startActivity(new Intent(this, SettingsActivity.class));
                        return true;
                    }
                    return false;
                });
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error initializing views: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(this, "Welcome to ExpiryTrack!", Toast.LENGTH_SHORT).show();
    }
}
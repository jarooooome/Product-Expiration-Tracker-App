package com.example.productexpirationtrackerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class ConsumedHistoryActivity extends AppCompatActivity {

    private RecyclerView historyRecyclerView;
    private ConsumedHistoryAdapter adapter;
    private ConsumedProductViewModel consumedProductViewModel;
    private UserRepository userRepository;

    private TextView tvConsumedCount, tvDiscardedCount, tvTotalCount;
    private ImageView backButton;
    private LinearLayout btnConsumed, btnDiscarded, btnAll;
    private View indicatorConsumed, indicatorDiscarded, indicatorAll;
    private LinearLayout mainLayout, headerLayout;

    private String currentFilter = "ALL"; // ALL, CONSUMED, DISCARDED
    private static final String TAG = "HISTORY_THEME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumed_history);

        // Initialize UserRepository for theme
        userRepository = new UserRepository(getApplication());

        // Initialize views
        initializeViews();

        // Apply theme with delay
        applyThemeFromDatabase();

        // Setup RecyclerView
        setupRecyclerView();

        // Initialize ViewModel
        consumedProductViewModel = new ViewModelProvider(this).get(ConsumedProductViewModel.class);

        // Setup observers
        setupObservers();

        // Setup click listeners
        setupClickListeners();

        // Update counts
        updateCounts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reapply theme when returning to this activity
        applyThemeFromDatabase();
        Log.d(TAG, "onResume called - theme reapplied");
    }

    private void initializeViews() {
        mainLayout = findViewById(R.id.mainLayout);
        headerLayout = findViewById(R.id.headerLayout);

        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        tvConsumedCount = findViewById(R.id.tvConsumedCount);
        tvDiscardedCount = findViewById(R.id.tvDiscardedCount);
        tvTotalCount = findViewById(R.id.tvTotalCount);
        backButton = findViewById(R.id.backButton);

        btnConsumed = findViewById(R.id.btnConsumed);
        btnDiscarded = findViewById(R.id.btnDiscarded);
        btnAll = findViewById(R.id.btnAll);

        indicatorConsumed = findViewById(R.id.indicatorConsumed);
        indicatorDiscarded = findViewById(R.id.indicatorDiscarded);
        indicatorAll = findViewById(R.id.indicatorAll);
    }

    // Apply theme from database
    private void applyThemeFromDatabase() {
        Log.d(TAG, "applyThemeFromDatabase called");

        // Add delay to ensure database is ready
        new Handler().postDelayed(() -> {
            User user = userRepository.getUserSync();

            if (user != null) {
                String theme = user.getColorTheme();
                Log.d(TAG, "✅ Theme from database after delay: " + theme);
                runOnUiThread(() -> applyThemeColors(theme));
            } else {
                // Fallback to SharedPreferences
                SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                String theme = prefs.getString("color_theme", "light");
                Log.d(TAG, "❌ No user found, using theme from prefs: " + theme);
                runOnUiThread(() -> applyThemeColors(theme));
            }
        }, 500);
    }

    // Apply theme colors - SIMPLIFIED to only light/dark
    private void applyThemeColors(String theme) {
        Log.d(TAG, "applyThemeColors called with theme: " + theme);

        boolean isDarkTheme = theme.equals("dark") || theme.equals("black");

        int primaryColor;
        int backgroundColor;
        int textColor;

        if (isDarkTheme) {
            // Dark theme colors
            primaryColor = ContextCompat.getColor(this, R.color.color_primary_dark);
            backgroundColor = ContextCompat.getColor(this, R.color.color_background_dark);
            textColor = ContextCompat.getColor(this, R.color.color_text_dark);
        } else {
            // Light theme colors
            primaryColor = ContextCompat.getColor(this, R.color.color_primary_light);
            backgroundColor = ContextCompat.getColor(this, R.color.color_background_light);
            textColor = ContextCompat.getColor(this, R.color.color_text_light);
        }

        Log.d(TAG, "Theme: " + (isDarkTheme ? "DARK" : "LIGHT"));
        Log.d(TAG, "Colors - Primary: " + primaryColor + ", BG: " + backgroundColor + ", Text: " + textColor);

        // Apply background to main layout
        if (mainLayout != null) {
            mainLayout.setBackgroundColor(backgroundColor);
            Log.d(TAG, "✅ Main layout background set");
        } else {
            Log.e(TAG, "❌ mainLayout is NULL!");
        }

        // Apply header color
        if (headerLayout != null) {
            headerLayout.setBackgroundColor(primaryColor);
            Log.d(TAG, "✅ Header layout background set");
        } else {
            Log.e(TAG, "❌ headerLayout is NULL!");
        }

        // Update filter tab colors
        updateFilterTabColors(isDarkTheme, primaryColor, textColor);
    }

    // Update filter tab colors - SIMPLIFIED
    private void updateFilterTabColors(boolean isDarkTheme, int primaryColor, int textColor) {
        // Update text colors for filter tabs
        if (btnAll != null && btnAll.getChildAt(0) instanceof TextView) {
            ((TextView) btnAll.getChildAt(0)).setTextColor(textColor);
        }
        if (btnConsumed != null && btnConsumed.getChildAt(0) instanceof TextView) {
            ((TextView) btnConsumed.getChildAt(0)).setTextColor(textColor);
        }
        if (btnDiscarded != null && btnDiscarded.getChildAt(0) instanceof TextView) {
            ((TextView) btnDiscarded.getChildAt(0)).setTextColor(textColor);
        }

        // Update indicator colors
        if (indicatorAll != null) {
            indicatorAll.setBackgroundColor(primaryColor);
        }
        if (indicatorConsumed != null) {
            indicatorConsumed.setBackgroundColor(primaryColor);
        }
        if (indicatorDiscarded != null) {
            indicatorDiscarded.setBackgroundColor(primaryColor);
        }
    }

    private void setupRecyclerView() {
        adapter = new ConsumedHistoryAdapter(this);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setAdapter(adapter);

        // Set item click listener
        adapter.setOnItemClickListener(new ConsumedHistoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ConsumedProduct product) {
                Toast.makeText(ConsumedHistoryActivity.this,
                        product.getProductName() + " - " + product.getActionType(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupObservers() {
        // Observe all products
        consumedProductViewModel.getAllConsumedProducts().observe(this, new Observer<List<ConsumedProduct>>() {
            @Override
            public void onChanged(List<ConsumedProduct> products) {
                if (currentFilter.equals("ALL")) {
                    adapter.setConsumedProducts(products);
                }
                updateCounts();
            }
        });

        // Observe consumed products
        consumedProductViewModel.getConsumedProducts().observe(this, new Observer<List<ConsumedProduct>>() {
            @Override
            public void onChanged(List<ConsumedProduct> products) {
                if (currentFilter.equals("CONSUMED")) {
                    adapter.setConsumedProducts(products);
                }
            }
        });

        // Observe discarded products
        consumedProductViewModel.getDiscardedProducts().observe(this, new Observer<List<ConsumedProduct>>() {
            @Override
            public void onChanged(List<ConsumedProduct> products) {
                if (currentFilter.equals("DISCARDED")) {
                    adapter.setConsumedProducts(products);
                }
            }
        });
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        btnAll.setOnClickListener(v -> {
            setActiveFilter("ALL");
            currentFilter = "ALL";
            adapter.setConsumedProducts(consumedProductViewModel.getAllConsumedProducts().getValue());
        });

        btnConsumed.setOnClickListener(v -> {
            setActiveFilter("CONSUMED");
            currentFilter = "CONSUMED";
            adapter.setConsumedProducts(consumedProductViewModel.getConsumedProducts().getValue());
        });

        btnDiscarded.setOnClickListener(v -> {
            setActiveFilter("DISCARDED");
            currentFilter = "DISCARDED";
            adapter.setConsumedProducts(consumedProductViewModel.getDiscardedProducts().getValue());
        });
    }

    private void setActiveFilter(String filter) {
        // Reset all indicators
        indicatorAll.setVisibility(View.GONE);
        indicatorConsumed.setVisibility(View.GONE);
        indicatorDiscarded.setVisibility(View.GONE);

        // Set active
        switch (filter) {
            case "ALL":
                indicatorAll.setVisibility(View.VISIBLE);
                break;
            case "CONSUMED":
                indicatorConsumed.setVisibility(View.VISIBLE);
                break;
            case "DISCARDED":
                indicatorDiscarded.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void updateCounts() {
        // Get counts from ViewModel
        consumedProductViewModel.getCount(new ConsumedProductRepository.RepositoryCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                runOnUiThread(() -> tvTotalCount.setText(String.valueOf(count)));
            }

            @Override
            public void onError(Exception e) {
                tvTotalCount.setText("0");
            }
        });

        consumedProductViewModel.getCountByType("CONSUMED", new ConsumedProductRepository.RepositoryCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                runOnUiThread(() -> tvConsumedCount.setText(String.valueOf(count)));
            }

            @Override
            public void onError(Exception e) {
                tvConsumedCount.setText("0");
            }
        });

        consumedProductViewModel.getCountByType("DISCARDED", new ConsumedProductRepository.RepositoryCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                runOnUiThread(() -> tvDiscardedCount.setText(String.valueOf(count)));
            }

            @Override
            public void onError(Exception e) {
                tvDiscardedCount.setText("0");
            }
        });
    }
}
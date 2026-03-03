package com.example.productexpirationtrackerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
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

    private TextView tvConsumedCount, tvDiscardedCount, tvTotalCount;
    private ImageView backButton;
    private LinearLayout btnConsumed, btnDiscarded, btnAll;
    private View indicatorConsumed, indicatorDiscarded, indicatorAll;
    private LinearLayout mainLayout, headerLayout;

    private SharedPreferences preferences;
    private String currentFilter = "ALL";
    private static final String TAG = "HISTORY_THEME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumed_history);

        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        initializeViews();
        applyTheme();
        setupRecyclerView();

        consumedProductViewModel = new ViewModelProvider(this).get(ConsumedProductViewModel.class);

        setupObservers();
        setupClickListeners();
        updateCounts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyTheme();
    }

    // ── Views ─────────────────────────────────────────────────────────────────

    private void initializeViews() {
        mainLayout    = findViewById(R.id.mainLayout);
        headerLayout  = findViewById(R.id.headerLayout);

        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        tvConsumedCount     = findViewById(R.id.tvConsumedCount);
        tvDiscardedCount    = findViewById(R.id.tvDiscardedCount);
        tvTotalCount        = findViewById(R.id.tvTotalCount);
        backButton          = findViewById(R.id.backButton);

        btnAll      = findViewById(R.id.btnAll);
        btnConsumed = findViewById(R.id.btnConsumed);
        btnDiscarded = findViewById(R.id.btnDiscarded);

        indicatorAll      = findViewById(R.id.indicatorAll);
        indicatorConsumed = findViewById(R.id.indicatorConsumed);
        indicatorDiscarded = findViewById(R.id.indicatorDiscarded);
    }

    // ── Theme ─────────────────────────────────────────────────────────────────

    private void applyTheme() {
        String theme = preferences.getString("color_theme", "white");
        boolean isDark = "black".equals(theme);
        float dp = getResources().getDisplayMetrics().density;

        // ── Palette ───────────────────────────────────────────────────────────
        int mainBg       = isDark ? Color.parseColor("#121212") : Color.parseColor("#F7F7F7");
        int headerBg     = isDark ? Color.parseColor("#121212") : Color.parseColor("#F7F7F7");
        int titleColor   = isDark ? Color.WHITE                 : Color.parseColor("#1A1A1A");
        int labelColor   = isDark ? Color.parseColor("#888888") : Color.parseColor("#888888");
        int dividerColor = isDark ? Color.parseColor("#2A2A2A") : Color.parseColor("#E0E0E0");
        int tabTextActive   = isDark ? Color.WHITE              : Color.parseColor("#1A1A1A");
        int tabTextInactive = isDark ? Color.parseColor("#666666") : Color.parseColor("#AAAAAA");
        int accentGreen  = isDark ? Color.parseColor("#4CAF50") : Color.parseColor("#388E3C");
        int statCardBg   = isDark ? Color.parseColor("#1E1E1E") : Color.WHITE;
        int statBorder   = isDark ? Color.parseColor("#2E2E2E") : Color.parseColor("#EEEEEE");
        int statTotalColor = isDark ? Color.WHITE               : Color.parseColor("#1A1A1A");

        // ── Root background ───────────────────────────────────────────────────
        if (mainLayout != null) mainLayout.setBackgroundColor(mainBg);
        if (headerLayout != null) headerLayout.setBackgroundColor(headerBg);

        // ── Back arrow ────────────────────────────────────────────────────────
        if (backButton != null)
            backButton.setImageTintList(
                    android.content.res.ColorStateList.valueOf(titleColor));

        // ── Title ─────────────────────────────────────────────────────────────
        TextView titleView = findViewById(R.id.titleTextView);
        if (titleView != null) titleView.setTextColor(titleColor);

        // ── Stat card backgrounds ─────────────────────────────────────────────
        int[] statCardIds = { R.id.statCardTotal, R.id.statCardConsumed, R.id.statCardDiscarded };
        for (int id : statCardIds) {
            View card = findViewById(id);
            if (card != null) {
                GradientDrawable bg = new GradientDrawable();
                bg.setColor(statCardBg);
                bg.setCornerRadius(10 * dp);
                bg.setStroke(1, statBorder);
                card.setBackground(bg);
            }
        }

        // ── Total stat number + label ─────────────────────────────────────────
        if (tvTotalCount != null) tvTotalCount.setTextColor(statTotalColor);
        TextView tvTotalLabel = findViewById(R.id.tvTotalLabel);
        if (tvTotalLabel != null) tvTotalLabel.setTextColor(labelColor);

        // ── Consumed / Discarded stat colors stay fixed (green/red always) ────
        // tvConsumedCount and tvDiscardedCount keep their green/red from XML

        // ── Stat dividers ─────────────────────────────────────────────────────
        View sd1 = findViewById(R.id.statDivider1);
        View sd2 = findViewById(R.id.statDivider2);
        if (sd1 != null) sd1.setBackgroundColor(dividerColor);
        if (sd2 != null) sd2.setBackgroundColor(dividerColor);

        // ── Horizontal rule dividers ──────────────────────────────────────────
        View headerDivider = findViewById(R.id.headerDivider);
        View tabDivider    = findViewById(R.id.tabDivider);
        if (headerDivider != null) headerDivider.setBackgroundColor(dividerColor);
        if (tabDivider != null)    tabDivider.setBackgroundColor(dividerColor);

        // ── Filter tabs ───────────────────────────────────────────────────────
        // "All" tab is active by default
        setTabTextColor(R.id.tvAll,      currentFilter.equals("ALL")      ? tabTextActive : tabTextInactive);
        setTabTextColor(R.id.tvConsumed, currentFilter.equals("CONSUMED") ? tabTextActive : tabTextInactive);
        setTabTextColor(R.id.tvDiscarded,currentFilter.equals("DISCARDED")? tabTextActive : tabTextInactive);

        // Active indicator = green accent
        if (indicatorAll != null)       indicatorAll.setBackgroundColor(accentGreen);
        if (indicatorConsumed != null)  indicatorConsumed.setBackgroundColor(accentGreen);
        if (indicatorDiscarded != null) indicatorDiscarded.setBackgroundColor(accentGreen);

        // ── Filter layout background ──────────────────────────────────────────
        LinearLayout filterLayout = findViewById(R.id.filterLayout);
        if (filterLayout != null) filterLayout.setBackgroundColor(mainBg);

        // ── Empty state text ──────────────────────────────────────────────────
        TextView emptyTitle = findViewById(R.id.emptyTitleText);
        TextView emptySub   = findViewById(R.id.emptySubText);
        if (emptyTitle != null) emptyTitle.setTextColor(isDark ? Color.parseColor("#555555") : Color.parseColor("#AAAAAA"));
        if (emptySub   != null) emptySub.setTextColor(isDark ? Color.parseColor("#444444") : Color.parseColor("#BBBBBB"));

        Log.d(TAG, "Theme applied: " + theme);
    }

    private void setTabTextColor(int viewId, int color) {
        TextView tv = findViewById(viewId);
        if (tv != null) tv.setTextColor(color);
    }

    // ── RecyclerView ──────────────────────────────────────────────────────────

    private void setupRecyclerView() {
        adapter = new ConsumedHistoryAdapter(this);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(product ->
                Toast.makeText(ConsumedHistoryActivity.this,
                        product.getProductName() + " - " + product.getActionType(),
                        Toast.LENGTH_SHORT).show()
        );
    }

    // ── Observers ─────────────────────────────────────────────────────────────

    private void setupObservers() {
        consumedProductViewModel.getAllConsumedProducts().observe(this, products -> {
            if (currentFilter.equals("ALL")) adapter.setConsumedProducts(products);
            updateCounts();
        });

        consumedProductViewModel.getConsumedProducts().observe(this, products -> {
            if (currentFilter.equals("CONSUMED")) adapter.setConsumedProducts(products);
        });

        consumedProductViewModel.getDiscardedProducts().observe(this, products -> {
            if (currentFilter.equals("DISCARDED")) adapter.setConsumedProducts(products);
        });
    }

    // ── Click listeners ───────────────────────────────────────────────────────

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        btnAll.setOnClickListener(v -> {
            currentFilter = "ALL";
            setActiveFilter("ALL");
            adapter.setConsumedProducts(consumedProductViewModel.getAllConsumedProducts().getValue());
        });

        btnConsumed.setOnClickListener(v -> {
            currentFilter = "CONSUMED";
            setActiveFilter("CONSUMED");
            adapter.setConsumedProducts(consumedProductViewModel.getConsumedProducts().getValue());
        });

        btnDiscarded.setOnClickListener(v -> {
            currentFilter = "DISCARDED";
            setActiveFilter("DISCARDED");
            adapter.setConsumedProducts(consumedProductViewModel.getDiscardedProducts().getValue());
        });
    }

    private void setActiveFilter(String filter) {
        String theme = preferences.getString("color_theme", "white");
        boolean isDark = "black".equals(theme);
        int active   = isDark ? Color.WHITE                 : Color.parseColor("#1A1A1A");
        int inactive = isDark ? Color.parseColor("#666666") : Color.parseColor("#AAAAAA");

        // Reset all tab text to inactive
        setTabTextColor(R.id.tvAll,       inactive);
        setTabTextColor(R.id.tvConsumed,  inactive);
        setTabTextColor(R.id.tvDiscarded, inactive);

        // Hide all indicators
        if (indicatorAll != null)       indicatorAll.setVisibility(View.GONE);
        if (indicatorConsumed != null)  indicatorConsumed.setVisibility(View.GONE);
        if (indicatorDiscarded != null) indicatorDiscarded.setVisibility(View.GONE);

        // Activate the selected one
        switch (filter) {
            case "ALL":
                setTabTextColor(R.id.tvAll, active);
                if (indicatorAll != null) indicatorAll.setVisibility(View.VISIBLE);
                break;
            case "CONSUMED":
                setTabTextColor(R.id.tvConsumed, active);
                if (indicatorConsumed != null) indicatorConsumed.setVisibility(View.VISIBLE);
                break;
            case "DISCARDED":
                setTabTextColor(R.id.tvDiscarded, active);
                if (indicatorDiscarded != null) indicatorDiscarded.setVisibility(View.VISIBLE);
                break;
        }
    }

    // ── Counts ────────────────────────────────────────────────────────────────

    private void updateCounts() {
        consumedProductViewModel.getCount(new ConsumedProductRepository.RepositoryCallback<Integer>() {
            @Override public void onSuccess(Integer count) {
                runOnUiThread(() -> { if (tvTotalCount != null) tvTotalCount.setText(String.valueOf(count)); });
            }
            @Override public void onError(Exception e) {
                if (tvTotalCount != null) tvTotalCount.setText("0");
            }
        });

        consumedProductViewModel.getCountByType("CONSUMED", new ConsumedProductRepository.RepositoryCallback<Integer>() {
            @Override public void onSuccess(Integer count) {
                runOnUiThread(() -> { if (tvConsumedCount != null) tvConsumedCount.setText(String.valueOf(count)); });
            }
            @Override public void onError(Exception e) {
                if (tvConsumedCount != null) tvConsumedCount.setText("0");
            }
        });

        consumedProductViewModel.getCountByType("DISCARDED", new ConsumedProductRepository.RepositoryCallback<Integer>() {
            @Override public void onSuccess(Integer count) {
                runOnUiThread(() -> { if (tvDiscardedCount != null) tvDiscardedCount.setText(String.valueOf(count)); });
            }
            @Override public void onError(Exception e) {
                if (tvDiscardedCount != null) tvDiscardedCount.setText("0");
            }
        });
    }
}
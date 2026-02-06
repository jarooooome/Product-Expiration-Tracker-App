package com.example.productexpirationtrackerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat notificationSwitch;
    private SeekBar alertDaysSeekBar;
    private TextView alertDaysValue;
    private Button lightButton;
    private Button darkButton;
    private CardView dataExportCard;
    private CardView accountSyncCard;
    private CardView dataUseCard;
    private CardView helpCard;
    private BottomNavigationView bottomNavigation;

    private SharedPreferences preferences;
    private static final String PREFS_NAME = "AppSettings";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";
    private static final String KEY_ALERT_DAYS = "alert_days";
    private static final String KEY_THEME = "theme_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeViews();
        loadPreferences();
        setupListeners();
        setupBottomNavigation();
    }

    private void initializeViews() {
        notificationSwitch = findViewById(R.id.notificationSwitch);
        alertDaysSeekBar = findViewById(R.id.alertDaysSeekBar);
        alertDaysValue = findViewById(R.id.alertDaysValue);
        lightButton = findViewById(R.id.lightButton);
        darkButton = findViewById(R.id.darkButton);
        dataExportCard = findViewById(R.id.dataExportCard);
        accountSyncCard = findViewById(R.id.accountSyncCard);
        dataUseCard = findViewById(R.id.dataUseCard);
        helpCard = findViewById(R.id.helpCard);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void loadPreferences() {
        // Load notification setting
        boolean notificationsEnabled = preferences.getBoolean(KEY_NOTIFICATIONS, true);
        notificationSwitch.setChecked(notificationsEnabled);

        // Load alert days
        int alertDays = preferences.getInt(KEY_ALERT_DAYS, 3);
        alertDaysSeekBar.setProgress(alertDays);
        updateAlertDaysText(alertDays);

        // Load theme
        String theme = preferences.getString(KEY_THEME, "dark");
        updateThemeButtons(theme);
    }

    private void setupListeners() {
        // Notification switch
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean(KEY_NOTIFICATIONS, isChecked).apply();
            Toast.makeText(this,
                    isChecked ? getString(R.string.notifications_enabled) : getString(R.string.notifications_disabled),
                    Toast.LENGTH_SHORT).show();
        });

        // Alert days seekbar
        alertDaysSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateAlertDaysText(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int days = seekBar.getProgress();
                preferences.edit().putInt(KEY_ALERT_DAYS, days).apply();
                Toast.makeText(SettingsActivity.this,
                        getString(R.string.alert_set_to_days, days),
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Theme buttons
        lightButton.setOnClickListener(v -> {
            setTheme("light");
            updateThemeButtons("light");
        });

        darkButton.setOnClickListener(v -> {
            setTheme("dark");
            updateThemeButtons("dark");
        });

        // Other cards
        dataExportCard.setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.data_export_coming_soon), Toast.LENGTH_SHORT).show();
            // TODO: Implement data export
        });

        accountSyncCard.setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.account_sync_coming_soon), Toast.LENGTH_SHORT).show();
            // TODO: Implement account sync
        });

        dataUseCard.setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.data_usage_coming_soon), Toast.LENGTH_SHORT).show();
            // TODO: Implement data usage settings
        });

        helpCard.setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.help_center_coming_soon), Toast.LENGTH_SHORT).show();
            // TODO: Implement help center
        });
    }

    private void updateAlertDaysText(int days) {
        alertDaysValue.setText(getString(R.string.days_before_expiration, days));
    }

    private void setTheme(String theme) {
        preferences.edit().putString(KEY_THEME, theme).apply();
        Toast.makeText(this,
                theme.equals("dark") ? getString(R.string.dark_theme_applied) : getString(R.string.light_theme_applied),
                Toast.LENGTH_SHORT).show();
        // TODO: Actually implement theme switching
    }

    private void updateThemeButtons(String theme) {
        if (theme.equals("light")) {
            lightButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            lightButton.setTextColor(getResources().getColor(android.R.color.white));
            darkButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            darkButton.setTextColor(getResources().getColor(android.R.color.black));
        } else {
            darkButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            darkButton.setTextColor(getResources().getColor(android.R.color.white));
            lightButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            lightButton.setTextColor(getResources().getColor(android.R.color.black));
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_settings);

        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_inventory) {
                startActivity(new Intent(SettingsActivity.this, InventoryActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_settings) {
                return true;
            }

            return false;
        });
    }
}
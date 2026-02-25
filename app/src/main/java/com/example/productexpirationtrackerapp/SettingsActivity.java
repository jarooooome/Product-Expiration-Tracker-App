package com.example.productexpirationtrackerapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {

    private SeekBar reminderFrequencySeekBar;
    private TextView frequencyValueText;
    private RadioGroup vibrationRadioGroup;
    private RadioGroup themeRadioGroup;
    private TextView notificationTimeText;
    private Button notificationTimeButton;
    private Button saveButton;
    private Button cancelButton;

    // Bottom Navigation
    private LinearLayout navProfile;
    private LinearLayout navProducts;
    private LinearLayout navSettings;
    private ImageView navProfileIcon;
    private ImageView navProductsIcon;
    private ImageView navSettingsIcon;
    private TextView navProfileText;
    private TextView navProductsText;
    private TextView navSettingsText;
    private View navProfileIndicator;
    private View navProductsIndicator;
    private View navSettingsIndicator;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public static final String PREF_REMINDER_DAYS = "reminder_days";
    public static final String PREF_VIBRATION_PATTERN = "vibration_pattern";
    public static final String PREF_THEME = "color_theme";
    public static final String PREF_NOTIFICATION_HOUR = "notification_hour";
    public static final String PREF_NOTIFICATION_MINUTE = "notification_minute";

    public static final int DEFAULT_REMINDER_DAYS = 3;
    public static final String DEFAULT_VIBRATION_PATTERN = "default";
    public static final String DEFAULT_THEME = "white";
    public static final int DEFAULT_NOTIFICATION_HOUR = 9;
    public static final int DEFAULT_NOTIFICATION_MINUTE = 0;

    private int selectedHour = DEFAULT_NOTIFICATION_HOUR;
    private int selectedMinute = DEFAULT_NOTIFICATION_MINUTE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        editor = preferences.edit();

        initializeViews();
        initializeBottomNavigation();
        setupBottomNavigation();
        setupSeekBar();
        setupTimePicker();
        loadCurrentSettings();
        setupClickListeners();
    }

    private void initializeViews() {
        reminderFrequencySeekBar = findViewById(R.id.reminderFrequencySeekBar);
        frequencyValueText = findViewById(R.id.frequencyValueText);
        vibrationRadioGroup = findViewById(R.id.vibrationRadioGroup);
        themeRadioGroup = findViewById(R.id.themeRadioGroup);
        notificationTimeText = findViewById(R.id.notificationTimeText);
        notificationTimeButton = findViewById(R.id.notificationTimeButton);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
    }

    private void initializeBottomNavigation() {
        try {
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

            Log.d("SettingsActivity", "Bottom navigation initialized");
        } catch (Exception e) {
            Log.e("SettingsActivity", "Error initializing bottom nav: " + e.getMessage());
        }
    }

    private void setupBottomNavigation() {
        // Set Settings as active
        setActiveNavItem(navSettings, navSettingsIcon, navSettingsText, navSettingsIndicator);

        // Set Profile and Products as inactive
        setInactiveNavItem(navProfile, navProfileIcon, navProfileText, navProfileIndicator);
        setInactiveNavItem(navProducts, navProductsIcon, navProductsText, navProductsIndicator);

        // Profile click listener
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Log.d("SettingsActivity", "Profile navigation clicked");
                Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            });
        }

        // Products click listener
        if (navProducts != null) {
            navProducts.setOnClickListener(v -> {
                Log.d("SettingsActivity", "Products navigation clicked");
                Intent intent = new Intent(SettingsActivity.this, ProductListActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            });
        }

        // Settings click listener (already on this screen)
        if (navSettings != null) {
            navSettings.setOnClickListener(v -> {
                Log.d("SettingsActivity", "Settings navigation clicked - already on this screen");
            });
        }
    }

    private void setActiveNavItem(LinearLayout navItem, ImageView icon, TextView text, View indicator) {
        if (icon != null) {
            icon.setImageTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
        }
        if (text != null) {
            text.setTextColor(Color.WHITE);
            text.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        if (indicator != null) {
            indicator.setVisibility(View.VISIBLE);
        }
    }

    private void setInactiveNavItem(LinearLayout navItem, ImageView icon, TextView text, View indicator) {
        if (icon != null) {
            icon.setImageTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
        }
        if (text != null) {
            text.setTextColor(Color.WHITE);
            text.setAlpha(0.8f);
            text.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
        if (indicator != null) {
            indicator.setVisibility(View.GONE);
        }
    }

    private void setupSeekBar() {
        reminderFrequencySeekBar.setMax(30);
        reminderFrequencySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String displayText;
                if (progress == 0) {
                    displayText = "Same day (0 days before)";
                } else if (progress == 1) {
                    displayText = "1 day before";
                } else {
                    displayText = progress + " days before";
                }
                frequencyValueText.setText(displayText);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupTimePicker() {
        notificationTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });
    }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        selectedHour = hourOfDay;
                        selectedMinute = minute;
                        updateTimeDisplay();
                    }
                },
                selectedHour,
                selectedMinute,
                true // 24-hour format
        );
        timePickerDialog.show();
    }

    private void updateTimeDisplay() {
        String timeString = String.format("%02d:%02d", selectedHour, selectedMinute);
        String amPm;
        if (selectedHour < 12) {
            amPm = "AM";
            int displayHour = selectedHour == 0 ? 12 : selectedHour;
            timeString = String.format("%d:%02d %s", displayHour, selectedMinute, amPm);
        } else {
            amPm = "PM";
            int displayHour = selectedHour == 12 ? 12 : selectedHour - 12;
            timeString = String.format("%d:%02d %s", displayHour, selectedMinute, amPm);
        }
        notificationTimeText.setText("⏰ " + timeString);
    }

    private void loadCurrentSettings() {
        int currentDays = preferences.getInt(PREF_REMINDER_DAYS, DEFAULT_REMINDER_DAYS);
        reminderFrequencySeekBar.setProgress(currentDays);

        String vibrationPattern = preferences.getString(PREF_VIBRATION_PATTERN, DEFAULT_VIBRATION_PATTERN);
        switch (vibrationPattern) {
            case "none":
                vibrationRadioGroup.check(R.id.vibration_none);
                break;
            case "short":
                vibrationRadioGroup.check(R.id.vibration_short);
                break;
            case "long":
                vibrationRadioGroup.check(R.id.vibration_long);
                break;
            case "default":
            default:
                vibrationRadioGroup.check(R.id.vibration_default);
                break;
        }

        // Load theme preference - UPDATED to only White and Black
        String theme = preferences.getString(PREF_THEME, DEFAULT_THEME);
        switch (theme) {
            case "white":
                themeRadioGroup.check(R.id.theme_white);
                break;
            case "black":
                themeRadioGroup.check(R.id.theme_black);
                break;
            default:
                themeRadioGroup.check(R.id.theme_white);
                break;
        }

        // Load notification time
        selectedHour = preferences.getInt(PREF_NOTIFICATION_HOUR, DEFAULT_NOTIFICATION_HOUR);
        selectedMinute = preferences.getInt(PREF_NOTIFICATION_MINUTE, DEFAULT_NOTIFICATION_MINUTE);
        updateTimeDisplay();

        String displayText;
        if (currentDays == 0) {
            displayText = "Same day (0 days before)";
        } else if (currentDays == 1) {
            displayText = "1 day before";
        } else {
            displayText = currentDays + " days before";
        }
        frequencyValueText.setText(displayText);
    }

    private void setupClickListeners() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void saveSettings() {
        int reminderDays = reminderFrequencySeekBar.getProgress();

        int selectedVibrationId = vibrationRadioGroup.getCheckedRadioButtonId();
        String vibrationPattern;
        if (selectedVibrationId == R.id.vibration_none) {
            vibrationPattern = "none";
        } else if (selectedVibrationId == R.id.vibration_short) {
            vibrationPattern = "short";
        } else if (selectedVibrationId == R.id.vibration_long) {
            vibrationPattern = "long";
        } else {
            vibrationPattern = "default";
        }

        // Get selected theme - UPDATED to only White and Black
        int selectedThemeId = themeRadioGroup.getCheckedRadioButtonId();
        String theme;
        if (selectedThemeId == R.id.theme_white) {
            theme = "white";
        } else if (selectedThemeId == R.id.theme_black) {
            theme = "black";
        } else {
            theme = "white";
        }

        // Save to SharedPreferences
        editor.putInt(PREF_REMINDER_DAYS, reminderDays);
        editor.putString(PREF_VIBRATION_PATTERN, vibrationPattern);
        editor.putString(PREF_THEME, theme);
        editor.putInt(PREF_NOTIFICATION_HOUR, selectedHour);
        editor.putInt(PREF_NOTIFICATION_MINUTE, selectedMinute);
        editor.apply();

        // Save theme to database
        UserRepository userRepository = new UserRepository(getApplication());
        userRepository.updateTheme(theme);

        Log.d("SettingsActivity", "Theme saved to database: " + theme);

        // Recreate notification channel with new settings
        recreateNotificationChannel(vibrationPattern);

        Toast.makeText(this, "Settings saved to database!", Toast.LENGTH_LONG).show();
        finish();
    }

    private void recreateNotificationChannel(String vibrationPattern) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.deleteNotificationChannel("expiry_channel");

            NotificationChannel channel = new NotificationChannel(
                    "expiry_channel",
                    "Expiry Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Get notified when products are about to expire");

            if (vibrationPattern.equals("none")) {
                channel.enableVibration(false);
            } else {
                channel.enableVibration(true);
                switch (vibrationPattern) {
                    case "short":
                        channel.setVibrationPattern(new long[]{0, 200, 100, 200});
                        break;
                    case "long":
                        channel.setVibrationPattern(new long[]{0, 800, 200, 800});
                        break;
                    case "default":
                    default:
                        channel.setVibrationPattern(new long[]{0, 500, 200, 500});
                        break;
                }
            }

            manager.createNotificationChannel(channel);
            Log.d("NOTIF_DEBUG", "✅ Recreated notification channel");
        }
    }
}
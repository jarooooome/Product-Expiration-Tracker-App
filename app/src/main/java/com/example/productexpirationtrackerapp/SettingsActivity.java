package com.example.productexpirationtrackerapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    // Settings controls
    private SeekBar reminderFrequencySeekBar;
    private TextView frequencyValueText;
    private RadioGroup vibrationRadioGroup;
    private RadioGroup themeRadioGroup;
    private TextView notificationTimeText;
    private Button notificationTimeButton;
    private Button saveButton;
    private Button cancelButton;

    // Profile edit
    private TextView userNameText;
    private TextView editNameButton;
    private EditText nameEditText;
    private LinearLayout nameEditButtons;
    private Button saveNameButton;
    private Button cancelNameButton;
    private UserRepository userRepository;
    private ProductRepository productRepository;

    // Bottom nav
    private LinearLayout navProfile, navProducts, navSettings;
    private ImageView navProfileIcon, navProductsIcon, navSettingsIcon;
    private TextView navProfileText, navProductsText, navSettingsText;
    private View navProfileIndicator, navProductsIndicator, navSettingsIndicator;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public static final String PREF_REMINDER_DAYS      = "reminder_days";
    public static final String PREF_VIBRATION_PATTERN  = "vibration_pattern";
    public static final String PREF_THEME              = "color_theme";
    public static final String PREF_NOTIFICATION_HOUR  = "notification_hour";
    public static final String PREF_NOTIFICATION_MINUTE= "notification_minute";

    public static final int    DEFAULT_REMINDER_DAYS      = 3;
    public static final String DEFAULT_VIBRATION_PATTERN  = "default";
    public static final String DEFAULT_THEME              = "white";
    public static final int    DEFAULT_NOTIFICATION_HOUR  = 9;
    public static final int    DEFAULT_NOTIFICATION_MINUTE= 0;

    private int selectedHour   = DEFAULT_NOTIFICATION_HOUR;
    private int selectedMinute = DEFAULT_NOTIFICATION_MINUTE;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply background color synchronously from SharedPrefs BEFORE setContentView to prevent blink
        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String quickTheme = preferences.getString("color_theme", "white");
        int quickBg = "black".equals(quickTheme)
                ? android.graphics.Color.parseColor("#121212")
                : android.graphics.Color.parseColor("#F5F5F5");
        getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(quickBg));

        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        editor = preferences.edit();
        userRepository = new UserRepository(getApplication());
        productRepository = new ProductRepository(getApplication());

        initializeViews();
        applyTheme();
        setupBottomNavigation();
        setupSeekBar();
        setupTimePicker();
        loadCurrentSettings();
        loadUserName();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyTheme();
    }

    // ── View init ─────────────────────────────────────────────────────────────

    private void initializeViews() {
        reminderFrequencySeekBar = findViewById(R.id.reminderFrequencySeekBar);
        frequencyValueText       = findViewById(R.id.frequencyValueText);
        vibrationRadioGroup      = findViewById(R.id.vibrationRadioGroup);
        themeRadioGroup          = findViewById(R.id.themeRadioGroup);
        notificationTimeText     = findViewById(R.id.notificationTimeText);
        notificationTimeButton   = findViewById(R.id.notificationTimeButton);
        saveButton               = findViewById(R.id.saveButton);
        cancelButton             = findViewById(R.id.cancelButton);

        userNameText      = findViewById(R.id.userNameText);
        editNameButton    = (TextView) findViewById(R.id.editNameButton);
        nameEditText      = findViewById(R.id.nameEditText);
        nameEditButtons   = findViewById(R.id.nameEditButtons);
        saveNameButton    = findViewById(R.id.saveNameButton);
        cancelNameButton  = findViewById(R.id.cancelNameButton);

        navProfile   = findViewById(R.id.navProfile);
        navProducts  = findViewById(R.id.navProducts);
        navSettings  = findViewById(R.id.navSettings);

        navProfileIcon   = findViewById(R.id.navProfileIcon);
        navProductsIcon  = findViewById(R.id.navProductsIcon);
        navSettingsIcon  = findViewById(R.id.navSettingsIcon);

        navProfileText   = findViewById(R.id.navProfileText);
        navProductsText  = findViewById(R.id.navProductsText);
        navSettingsText  = findViewById(R.id.navSettingsText);

        navProfileIndicator  = findViewById(R.id.navProfileIndicator);
        navProductsIndicator = findViewById(R.id.navProductsIndicator);
        navSettingsIndicator = findViewById(R.id.navSettingsIndicator);
    }

    // ── Theme ─────────────────────────────────────────────────────────────────

    private void applyTheme() {
        String theme = preferences.getString(PREF_THEME, DEFAULT_THEME);
        boolean isDark = "black".equals(theme);
        float dp = getResources().getDisplayMetrics().density;

        // ── Palette ───────────────────────────────────────────────────────────
        int mainBg        = isDark ? Color.parseColor("#121212") : Color.parseColor("#F7F7F7");
        int titleColor    = isDark ? Color.WHITE                 : Color.parseColor("#1A1A1A");
        int sectionLabel  = isDark ? Color.parseColor("#888888") : Color.parseColor("#888888");
        int bodyText      = isDark ? Color.WHITE                 : Color.parseColor("#1A1A1A");
        int subText       = isDark ? Color.parseColor("#888888") : Color.parseColor("#888888");
        int hintColor     = isDark ? Color.parseColor("#555555") : Color.parseColor("#AAAAAA");
        int fieldBg       = isDark ? Color.parseColor("#222222") : Color.WHITE;
        int fieldBorder   = isDark ? Color.parseColor("#3A3A3A") : Color.parseColor("#DEDEDE");
        int dividerColor  = isDark ? Color.parseColor("#2A2A2A") : Color.parseColor("#E8E8E8");
        int accentGreen   = isDark ? Color.parseColor("#4CAF50") : Color.parseColor("#388E3C");
        int navBg         = isDark ? Color.parseColor("#1A1A1A") : Color.WHITE;
        int navActive     = isDark ? Color.WHITE                 : Color.parseColor("#1A1A1A");
        int navInactive   = isDark ? Color.WHITE                 : Color.parseColor("#1A1A1A");
        int navBorder     = isDark ? Color.parseColor("#2A2A2A") : Color.parseColor("#E8E8E8");

        // ── Root + scroll background ──────────────────────────────────────────
        View rootLayout = findViewById(R.id.rootLayout);
        if (rootLayout != null) rootLayout.setBackgroundColor(mainBg);
        LinearLayout mainLayout = findViewById(R.id.mainLayout);
        if (mainLayout != null) mainLayout.setBackgroundColor(mainBg);
        View scrollView = findViewById(R.id.scrollView);
        if (scrollView != null) scrollView.setBackgroundColor(mainBg);

        // ── Page title ────────────────────────────────────────────────────────
        TextView pageTitle = findViewById(R.id.pageTitleText);
        if (pageTitle != null) pageTitle.setTextColor(titleColor);

        // ── Section labels ────────────────────────────────────────────────────
        int[] sectionLabelIds = {
                R.id.sectionLabelProfile, R.id.sectionLabelReminder,
                R.id.sectionLabelVibration, R.id.sectionLabelTheme, R.id.sectionLabelNotif
        };
        for (int id : sectionLabelIds) {
            TextView tv = findViewById(id);
            if (tv != null) tv.setTextColor(sectionLabel);
        }

        // ── Description texts ─────────────────────────────────────────────────
        int[] descIds = {
                R.id.reminderDescText, R.id.vibrationDescText,
                R.id.themeDescText, R.id.notifDescText
        };
        for (int id : descIds) {
            TextView tv = findViewById(id);
            if (tv != null) tv.setTextColor(subText);
        }

        // ── SeekBar range labels ──────────────────────────────────────────────
        int[] rangeTextViews = { R.id.nameLabelText };
        for (int id : rangeTextViews) {
            TextView tv = findViewById(id);
            if (tv != null) tv.setTextColor(subText);
        }

        // ── Version text ──────────────────────────────────────────────────────
        TextView versionTv = findViewById(R.id.versionText);
        if (versionTv != null) versionTv.setTextColor(subText);

        // ── Dividers ──────────────────────────────────────────────────────────
        int[] dividerIds = { R.id.divider1, R.id.divider2, R.id.divider3, R.id.divider4, R.id.divider5 };
        for (int id : dividerIds) {
            View v = findViewById(id);
            if (v != null) v.setBackgroundColor(dividerColor);
        }

        // ── User name ─────────────────────────────────────────────────────────
        if (userNameText != null) userNameText.setTextColor(bodyText);

        // ── Edit label color ──────────────────────────────────────────────────
        if (editNameButton != null) editNameButton.setTextColor(accentGreen);

        // ── Name edit field ───────────────────────────────────────────────────
        if (nameEditText != null) {
            nameEditText.setTextColor(bodyText);
            nameEditText.setHintTextColor(hintColor);
            GradientDrawable etBg = new GradientDrawable();
            etBg.setColor(fieldBg);
            etBg.setCornerRadius(8 * dp);
            etBg.setStroke(1, fieldBorder);
            nameEditText.setBackground(etBg);
        }

        // ── Save name button (green) ──────────────────────────────────────────
        if (saveNameButton != null) {
            GradientDrawable saveBg = new GradientDrawable();
            saveBg.setColor(accentGreen);
            saveBg.setCornerRadius(8 * dp);
            saveNameButton.setBackground(saveBg);
            saveNameButton.setTextColor(Color.WHITE);
        }

        // ── Cancel name button (text-only) ────────────────────────────────────
        if (cancelNameButton != null) {
            cancelNameButton.setBackgroundColor(Color.TRANSPARENT);
            cancelNameButton.setTextColor(bodyText);
        }

        // ── Frequency value text (green accent) ───────────────────────────────
        if (frequencyValueText != null) frequencyValueText.setTextColor(accentGreen);

        // ── SeekBar tint ──────────────────────────────────────────────────────
        if (reminderFrequencySeekBar != null) {
            reminderFrequencySeekBar.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(accentGreen));
            reminderFrequencySeekBar.setThumbTintList(
                    android.content.res.ColorStateList.valueOf(accentGreen));
        }

        // ── Notification time text (accent green) ─────────────────────────────
        if (notificationTimeText != null) notificationTimeText.setTextColor(accentGreen);

        // ── Change Time button (green) ────────────────────────────────────────
        if (notificationTimeButton != null) {
            GradientDrawable timeBtnBg = new GradientDrawable();
            timeBtnBg.setColor(accentGreen);
            timeBtnBg.setCornerRadius(8 * dp);
            notificationTimeButton.setBackground(timeBtnBg);
            notificationTimeButton.setTextColor(Color.WHITE);
        }

        // ── Cancel button (text-only) ─────────────────────────────────────────
        if (cancelButton != null) {
            cancelButton.setBackgroundColor(Color.TRANSPARENT);
            cancelButton.setTextColor(bodyText);
        }

        // ── Save Settings button (green) ──────────────────────────────────────
        if (saveButton != null) {
            GradientDrawable saveBg = new GradientDrawable();
            saveBg.setColor(accentGreen);
            saveBg.setCornerRadius(8 * dp);
            saveButton.setBackground(saveBg);
            saveButton.setTextColor(Color.WHITE);
        }

        // ── Radio buttons ─────────────────────────────────────────────────────
        styleRadioGroup(vibrationRadioGroup, bodyText, accentGreen);
        styleRadioGroup(themeRadioGroup, bodyText, accentGreen);

        // ── Bottom navigation ─────────────────────────────────────────────────
        LinearLayout bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            android.graphics.drawable.GradientDrawable navRounded = new android.graphics.drawable.GradientDrawable();
            navRounded.setColor(navBg);
            float[] navRadii = {48f, 48f, 48f, 48f, 0f, 0f, 0f, 0f};
            navRounded.setCornerRadii(navRadii);
            bottomNav.setBackground(navRounded);
            bottomNav.setPadding(0, (int)(1 * dp), 0, 0);
        }

        // Nav border via parent background
        View navBorderLine = new View(this);
        // Apply nav colors
        applyNavColors(navActive, navInactive, accentGreen, navBg, navBorder, dp);
    }

    private void styleRadioGroup(RadioGroup group, int textColor, int accentColor) {
        if (group == null) return;
        android.content.res.ColorStateList colorStateList = new android.content.res.ColorStateList(
                new int[][]{
                        new int[]{ android.R.attr.state_checked },
                        new int[]{ -android.R.attr.state_checked }
                },
                new int[]{ accentColor, textColor }
        );
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof RadioButton) {
                RadioButton rb = (RadioButton) child;
                rb.setTextColor(textColor);
                rb.setButtonTintList(colorStateList);
            }
        }
    }

    private void applyNavColors(int active, int inactive, int accent, int navBg, int border, float dp) {
        // Nav background — rounded top corners
        LinearLayout bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(navBg);
            float[] radii = {48f, 48f, 48f, 48f, 0f, 0f, 0f, 0f};
            bg.setCornerRadii(radii);
            bottomNav.setBackground(bg);
        }

        // Settings = active, others = inactive
        tintNavItem(navSettingsIcon, navSettingsText, navSettingsIndicator, active, accent, true);
        tintNavItem(navProfileIcon,  navProfileText,  navProfileIndicator,  inactive, accent, false);
        tintNavItem(navProductsIcon, navProductsText, navProductsIndicator, inactive, accent, false);
    }

    private void tintNavItem(ImageView icon, TextView text, View indicator,
                             int color, int accent, boolean isActive) {
        if (icon != null)
            icon.setImageTintList(android.content.res.ColorStateList.valueOf(color));
        if (text != null) {
            text.setTextColor(color);
            text.setTypeface(null, isActive ? Typeface.BOLD : Typeface.NORMAL);
        }
        if (indicator != null) {
            indicator.setVisibility(isActive ? View.VISIBLE : View.GONE);
            indicator.setBackgroundColor(accent);
        }
    }

    // ── Bottom nav setup ──────────────────────────────────────────────────────

    private void setupBottomNavigation() {
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
        if (navProducts != null) {
            navProducts.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProductListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
        // navSettings: already on this screen, do nothing
    }

    // ── SeekBar ───────────────────────────────────────────────────────────────

    private void setupSeekBar() {
        if (reminderFrequencySeekBar == null) return;
        reminderFrequencySeekBar.setMax(30);
        reminderFrequencySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String text;
                if (progress == 0)      text = "Same day (0 days before)";
                else if (progress == 1) text = "1 day before";
                else                    text = progress + " days before";
                if (frequencyValueText != null) frequencyValueText.setText(text);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    // ── Time picker ───────────────────────────────────────────────────────────

    private void setupTimePicker() {
        if (notificationTimeButton == null) return;
        notificationTimeButton.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, hour, minute) -> {
                selectedHour = hour;
                selectedMinute = minute;
                updateTimeDisplay();
            }, selectedHour, selectedMinute, false).show();
        });
    }

    private void updateTimeDisplay() {
        String amPm = selectedHour < 12 ? "AM" : "PM";
        int displayHour = selectedHour == 0 ? 12
                : selectedHour > 12 ? selectedHour - 12
                : selectedHour;
        String timeStr = String.format("%d:%02d %s", displayHour, selectedMinute, amPm);
        if (notificationTimeText != null) notificationTimeText.setText(timeStr);
    }

    // ── Load settings ─────────────────────────────────────────────────────────

    private void loadCurrentSettings() {
        int days = preferences.getInt(PREF_REMINDER_DAYS, DEFAULT_REMINDER_DAYS);
        if (reminderFrequencySeekBar != null) reminderFrequencySeekBar.setProgress(days);

        String vib = preferences.getString(PREF_VIBRATION_PATTERN, DEFAULT_VIBRATION_PATTERN);
        if (vibrationRadioGroup != null) {
            switch (vib) {
                case "none":  vibrationRadioGroup.check(R.id.vibration_none);  break;
                case "short": vibrationRadioGroup.check(R.id.vibration_short); break;
                case "long":  vibrationRadioGroup.check(R.id.vibration_long);  break;
                default:      vibrationRadioGroup.check(R.id.vibration_default); break;
            }
        }

        String theme = preferences.getString(PREF_THEME, DEFAULT_THEME);
        if (themeRadioGroup != null) {
            themeRadioGroup.check("black".equals(theme) ? R.id.theme_black : R.id.theme_white);
        }

        selectedHour   = preferences.getInt(PREF_NOTIFICATION_HOUR,   DEFAULT_NOTIFICATION_HOUR);
        selectedMinute = preferences.getInt(PREF_NOTIFICATION_MINUTE,  DEFAULT_NOTIFICATION_MINUTE);
        updateTimeDisplay();

        String displayText;
        if (days == 0)      displayText = "Same day (0 days before)";
        else if (days == 1) displayText = "1 day before";
        else                displayText = days + " days before";
        if (frequencyValueText != null) frequencyValueText.setText(displayText);
    }

    // ── User name ─────────────────────────────────────────────────────────────

    private void loadUserName() {
        userRepository.getUser(user -> {
            String name = null;
            if (user != null) name = user.getUserName();
            if (name == null || name.isEmpty())
                name = preferences.getString("user_name", "User");
            final String finalName = name;
            runOnUiThread(() -> {
                if (userNameText != null) userNameText.setText(finalName);
            });
        });
    }

    // ── Click listeners ───────────────────────────────────────────────────────

    private void setupClickListeners() {
        if (saveButton != null)   saveButton.setOnClickListener(v -> saveSettings());
        if (cancelButton != null) cancelButton.setOnClickListener(v -> finish());

        if (editNameButton != null) {
            editNameButton.setOnClickListener(v -> {
                if (nameEditText != null) {
                    nameEditText.setVisibility(View.VISIBLE);
                    nameEditText.setText(userNameText != null ? userNameText.getText() : "");
                    nameEditText.requestFocus();
                }
                if (nameEditButtons != null) nameEditButtons.setVisibility(View.VISIBLE);
                editNameButton.setVisibility(View.GONE);
            });
        }

        if (saveNameButton != null) {
            saveNameButton.setOnClickListener(v -> {
                if (nameEditText != null) {
                    String newName = nameEditText.getText().toString().trim();
                    if (newName.isEmpty()) {
                        Toast.makeText(this, "Nickname cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newName.length() > 8) {
                        Toast.makeText(this, "Nickname must be 8 letters or less", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Update UI immediately
                    if (userNameText != null) userNameText.setText(newName);
                    // Save to SharedPreferences
                    preferences.edit().putString("user_name", newName).apply();
                    // Save to database so ProductList greeting also updates
                    userRepository.getUser(user -> {
                        if (user != null) {
                            user.setUserName(newName);
                            userRepository.updateUser(user);
                        }
                    });
                    Toast.makeText(this, "Nickname updated!", Toast.LENGTH_SHORT).show();
                }
                hideNameEdit();
            });
        }

        if (cancelNameButton != null) {
            cancelNameButton.setOnClickListener(v -> hideNameEdit());
        }

        // Vibration preview — fire haptic when user selects a pattern
        if (vibrationRadioGroup != null) {
            vibrationRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                android.os.Vibrator vibrator =
                        (android.os.Vibrator) getSystemService(VIBRATOR_SERVICE);
                if (vibrator == null || !vibrator.hasVibrator()) return;

                if (checkedId == R.id.vibration_none) {
                    // No vibration — do nothing
                    return;
                }

                long[] pattern;
                if (checkedId == R.id.vibration_short) {
                    pattern = new long[]{0, 200, 100, 200};
                } else if (checkedId == R.id.vibration_long) {
                    pattern = new long[]{0, 800, 200, 800};
                } else {
                    // default
                    pattern = new long[]{0, 500, 200, 500};
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    android.os.VibrationEffect effect =
                            android.os.VibrationEffect.createWaveform(pattern, -1);
                    vibrator.vibrate(effect);
                } else {
                    vibrator.vibrate(pattern, -1);
                }
            });
        }
    }

    private void hideNameEdit() {
        if (nameEditText != null)    nameEditText.setVisibility(View.GONE);
        if (nameEditButtons != null) nameEditButtons.setVisibility(View.GONE);
        if (editNameButton != null)  editNameButton.setVisibility(View.VISIBLE);
    }

    // ── Save settings ─────────────────────────────────────────────────────────

    private void saveSettings() {
        int days = reminderFrequencySeekBar != null ? reminderFrequencySeekBar.getProgress() : DEFAULT_REMINDER_DAYS;

        String vib = "default";
        if (vibrationRadioGroup != null) {
            int vid = vibrationRadioGroup.getCheckedRadioButtonId();
            if (vid == R.id.vibration_none)  vib = "none";
            else if (vid == R.id.vibration_short) vib = "short";
            else if (vid == R.id.vibration_long)  vib = "long";
        }

        String theme = "white";
        if (themeRadioGroup != null) {
            theme = themeRadioGroup.getCheckedRadioButtonId() == R.id.theme_black ? "black" : "white";
        }

        editor.putInt(PREF_REMINDER_DAYS, days);
        editor.putString(PREF_VIBRATION_PATTERN, vib);
        editor.putString(PREF_THEME, theme);
        editor.putInt(PREF_NOTIFICATION_HOUR, selectedHour);
        editor.putInt(PREF_NOTIFICATION_MINUTE, selectedMinute);
        editor.apply();

        // Reschedule expiry-based notifications for all products with the new time
        // (Does NOT affect the immediate "product added" notification)
        productRepository.rescheduleAllNotifications();

        userRepository.updateTheme(theme);
        Log.d(TAG, "Settings saved. Theme: " + theme);

        recreateNotificationChannel(vib);
        Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void recreateNotificationChannel(String vibPattern) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mgr = getSystemService(NotificationManager.class);

            // Must delete before recreating — Android ignores setting changes on existing channels
            mgr.deleteNotificationChannel("expiry_channel");

            // Use a new channel ID each time so OS doesn't restore old cached settings
            String channelId = "expiry_channel_" + vibPattern;
            // Also delete any previous pattern-specific channel
            for (String old : new String[]{"expiry_channel_default","expiry_channel_short",
                    "expiry_channel_long","expiry_channel_none"}) {
                mgr.deleteNotificationChannel(old);
            }

            NotificationChannel ch = new NotificationChannel(
                    channelId, "Expiry Notifications", NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("Get notified when products are about to expire");

            if ("none".equals(vibPattern)) {
                ch.enableVibration(false);
                ch.setVibrationPattern(new long[]{0});
            } else {
                ch.enableVibration(true);
                switch (vibPattern) {
                    case "short": ch.setVibrationPattern(new long[]{0,200,100,200}); break;
                    case "long":  ch.setVibrationPattern(new long[]{0,800,200,800}); break;
                    default:      ch.setVibrationPattern(new long[]{0,500,200,500}); break;
                }
            }
            mgr.createNotificationChannel(ch);

            // Persist the channel ID so the notification scheduler uses the right one
            preferences.edit().putString("notification_channel_id", channelId).apply();
        }
    }
}
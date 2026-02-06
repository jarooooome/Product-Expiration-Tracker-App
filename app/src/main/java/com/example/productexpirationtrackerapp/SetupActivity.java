package com.example.productexpirationtrackerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class SetupActivity extends AppCompatActivity {

    private RadioGroup themeGroup;
    private SwitchCompat notificationSwitch;
    private EditText userNameEditText;
    private Button finishButton;
    private Button skipSetupButton;
    private Button debugDbButton;
    private SharedPreferences preferences;
    private FrameLayout mainBackground;

    private static final String PREFS_NAME = "AppPrefs";
    private static final String PREF_SETUP_COMPLETED = "setup_completed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize views
        themeGroup = findViewById(R.id.themeGroup);
        notificationSwitch = findViewById(R.id.notificationSwitch);
        userNameEditText = findViewById(R.id.userNameEditText);
        finishButton = findViewById(R.id.finishButton);
        skipSetupButton = findViewById(R.id.skipSetupButton);
        debugDbButton = findViewById(R.id.debugDbButton);
        mainBackground = findViewById(R.id.mainBackground);

        // Load saved preferences
        loadSavedPreferences();

        // Setup theme change listener
        themeGroup.setOnCheckedChangeListener((group, checkedId) ->
                applyThemeBackground(getSelectedTheme())
        );

        // Setup buttons
        finishButton.setOnClickListener(v -> {
            savePreferences();
            completeSetup();
        });

        skipSetupButton.setOnClickListener(v -> {
            skipSetup();
        });

        debugDbButton.setOnClickListener(v -> debugDatabase());
    }

    private void loadSavedPreferences() {
        String savedTheme = preferences.getString("color_theme", "gradient"); // Default to gradient
        setThemeSelection(savedTheme);

        boolean notifications = preferences.getBoolean("notifications", true);
        notificationSwitch.setChecked(notifications);

        String userName = preferences.getString("user_name", "");
        if (!userName.isEmpty() && !userName.equals("User")) {
            userNameEditText.setText(userName);
        }

        // Apply the saved theme background
        applyThemeBackground(savedTheme);
    }

    private void setThemeSelection(String theme) {
        int buttonId = -1;

        switch (theme) {
            case "white": buttonId = R.id.themeWhite; break;
            case "green": buttonId = R.id.themeGreen; break;
            case "blue": buttonId = R.id.themeBlue; break;
            case "pink": buttonId = R.id.themePink; break;
            case "purple": buttonId = R.id.themePurple; break;
            case "black": buttonId = R.id.themeBlack; break;
            case "gradient": // Default gradient theme
                // Don't select any radio button
                break;
        }

        if (buttonId != -1) {
            themeGroup.check(buttonId);
        }
    }

    private String getSelectedTheme() {
        int selectedId = themeGroup.getCheckedRadioButtonId();

        if (selectedId == R.id.themeWhite) return "white";
        else if (selectedId == R.id.themeGreen) return "green";
        else if (selectedId == R.id.themeBlue) return "blue";
        else if (selectedId == R.id.themePink) return "pink";
        else if (selectedId == R.id.themePurple) return "purple";
        else if (selectedId == R.id.themeBlack) return "black";

        return "gradient"; // Default is the gradient background
    }

    private void applyThemeBackground(String theme) {
        int gradientResId;

        switch (theme) {
            case "green":
                gradientResId = R.drawable.gradient_green;
                break;
            case "blue":
                gradientResId = R.drawable.gradient_blue;
                break;
            case "pink":
                gradientResId = R.drawable.gradient_pink;
                break;
            case "purple":
                gradientResId = R.drawable.gradient_purple;
                break;
            case "black":
                gradientResId = R.drawable.gradient_dark;
                break;
            case "white":
                gradientResId = R.drawable.gradient_white;
                break;
            case "gradient":
            default:
                gradientResId = R.drawable.onboarding_gradient;
                break;
        }

        // Apply gradient background
        mainBackground.setBackgroundResource(gradientResId);
    }

    private void savePreferences() {
        String userName = userNameEditText.getText().toString().trim();
        if (userName.isEmpty()) {
            userName = "User";
        }

        String selectedTheme = getSelectedTheme();
        boolean notifications = notificationSwitch.isChecked();

        Log.d("SETUP", "Saving preferences: Name=" + userName +
                ", Theme=" + selectedTheme + ", Notifications=" + notifications);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("user_name", userName);
        editor.putString("color_theme", selectedTheme);
        editor.putBoolean("notifications", notifications);
        editor.putBoolean(PREF_SETUP_COMPLETED, true);
        editor.apply();

        Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();
    }

    private void skipSetup() {
        String userName = userNameEditText.getText().toString().trim();
        if (userName.isEmpty()) {
            userName = "User";
        }

        // Save user name but keep default gradient theme
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("user_name", userName);
        editor.putString("color_theme", "gradient"); // Default gradient theme
        editor.putBoolean("notifications", notificationSwitch.isChecked());
        editor.putBoolean(PREF_SETUP_COMPLETED, true);
        editor.apply();

        Toast.makeText(this, "Using default theme", Toast.LENGTH_SHORT).show();

        // Go to HomeActivity
        Intent intent = new Intent(SetupActivity.this, HomeActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void debugDatabase() {
        StringBuilder debugInfo = new StringBuilder();
        debugInfo.append("📊 SETUP DEBUG\n\n");

        String spName = preferences.getString("user_name", "NOT SET");
        String spTheme = preferences.getString("color_theme", "NOT SET");
        boolean spNotif = preferences.getBoolean("notifications", false);

        debugInfo.append("📝 CURRENT SETTINGS:\n");
        debugInfo.append("   • Name: ").append(spName).append("\n");
        debugInfo.append("   • Theme: ").append(spTheme).append("\n");
        debugInfo.append("   • Notifications: ").append(spNotif ? "ON" : "OFF").append("\n");

        new android.app.AlertDialog.Builder(this)
                .setTitle("Setup Debug Info")
                .setMessage(debugInfo.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void completeSetup() {
        Toast.makeText(this, "Setup completed!", Toast.LENGTH_SHORT).show();

        // Apply final theme background
        String selectedTheme = getSelectedTheme();
        applyThemeBackground(selectedTheme);

        // Navigate to HomeActivity
        Intent intent = new Intent(SetupActivity.this, HomeActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
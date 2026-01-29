package com.example.productexpirationtrackerapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class SetupActivity extends AppCompatActivity {

    private RadioGroup themeGroup;
    private Switch notificationSwitch;
    private EditText userNameEditText;
    private Button finishButton;
    private Button debugDbButton;
    private SharedPreferences preferences;
    private AppDatabase appDatabase;
    private UserRepository userRepository;

    // Theme preview views
    private LinearLayout headerLayout;
    private TextView titleTextView;
    private TextView subtitleTextView;
    private LinearLayout mainLayout;

    // ADD THIS CONSTANT (must match MainActivity and ThemeUtils)
    private static final String PREFS_NAME = "AppPrefs";
    private static final String PREF_SETUP_COMPLETED = "setup_completed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        // Initialize Room database
        appDatabase = AppDatabase.getDatabase(this);
        userRepository = new UserRepository(getApplication());

        // Use the SAME preferences name as ThemeUtils
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize views
        themeGroup = findViewById(R.id.themeGroup);
        notificationSwitch = findViewById(R.id.notificationSwitch);
        userNameEditText = findViewById(R.id.userNameEditText);
        finishButton = findViewById(R.id.finishButton);
        debugDbButton = findViewById(R.id.debugDbButton);

        // Initialize layout containers for background changes
        mainLayout = findViewById(R.id.mainLayout);

        // Initialize theme preview views
        headerLayout = findViewById(R.id.headerLayout);
        titleTextView = findViewById(R.id.titleTextView);
        subtitleTextView = findViewById(R.id.subtitleTextView);

        // Load saved preferences
        loadSavedPreferences();

        // Set up theme change listener for real-time preview
        themeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                applyThemePreview(getSelectedTheme());
            }
        });

        // Apply initial theme preview
        applyThemePreview(getSelectedTheme());

        // Finish button
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePreferences();
                completeSetup();
            }
        });

        // Debug Database button
        debugDbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                debugDatabase();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Apply theme when activity resumes
        ThemeUtils.applyTheme(this);
    }

    private void loadSavedPreferences() {
        // Try to load from Room database first
        User user = userRepository.getUserSync();

        if (user != null) {
            Log.d("SETUP", "Loaded from Room database: " + user.getUserName());
            setThemeSelection(user.getColorTheme());
            notificationSwitch.setChecked(user.isNotifications());
            String userName = user.getUserName();
            if (userName != null && !userName.isEmpty() && !userName.equals("User")) {
                userNameEditText.setText(userName);
            }

            // Apply the saved theme
            applyThemePreview(user.getColorTheme());
        } else {
            Log.d("SETUP", "No data in Room database, loading from SharedPreferences");
            // Fall back to SharedPreferences
            String savedTheme = preferences.getString("color_theme", "white");
            setThemeSelection(savedTheme);

            boolean notifications = preferences.getBoolean("notifications", true);
            notificationSwitch.setChecked(notifications);

            String userName = preferences.getString("user_name", "");
            if (!userName.isEmpty() && !userName.equals("User")) {
                userNameEditText.setText(userName);
            }

            // Apply the saved theme
            applyThemePreview(savedTheme);
        }
    }

    private void setThemeSelection(String theme) {
        RadioButton selectedButton = null;

        switch (theme) {
            case "white":
                selectedButton = findViewById(R.id.themeWhite);
                break;
            case "green":
                selectedButton = findViewById(R.id.themeGreen);
                break;
            case "blue":
                selectedButton = findViewById(R.id.themeBlue);
                break;
            case "pink":
                selectedButton = findViewById(R.id.themePink);
                break;
            case "purple":
                selectedButton = findViewById(R.id.themePurple);
                break;
            case "black":
                selectedButton = findViewById(R.id.themeBlack);
                break;
        }

        if (selectedButton != null) {
            selectedButton.setChecked(true);
        }
    }

    private String getSelectedTheme() {
        int selectedId = themeGroup.getCheckedRadioButtonId();

        if (selectedId == R.id.themeWhite) {
            return "white";
        } else if (selectedId == R.id.themeGreen) {
            return "green";
        } else if (selectedId == R.id.themeBlue) {
            return "blue";
        } else if (selectedId == R.id.themePink) {
            return "pink";
        } else if (selectedId == R.id.themePurple) {
            return "purple";
        } else if (selectedId == R.id.themeBlack) {
            return "black";
        }

        return "white"; // WHITE is now default
    }

    private void applyThemePreview(String theme) {
        // Use ThemeUtils to get colors
        int primaryColor;
        int lightColor;
        int textColor;
        int backgroundColor;

        switch (theme) {
            case "green":
                primaryColor = getResources().getColor(R.color.color_primary_green);
                lightColor = getResources().getColor(R.color.color_primary_light_green);
                textColor = getResources().getColor(R.color.color_text_green);
                backgroundColor = getResources().getColor(R.color.color_background_green);
                break;
            case "blue":
                primaryColor = getResources().getColor(R.color.color_primary_blue);
                lightColor = getResources().getColor(R.color.color_primary_light_blue);
                textColor = getResources().getColor(R.color.color_text_blue);
                backgroundColor = getResources().getColor(R.color.color_background_blue);
                break;
            case "pink":
                primaryColor = getResources().getColor(R.color.color_primary_pink);
                lightColor = getResources().getColor(R.color.color_primary_light_pink);
                textColor = getResources().getColor(R.color.color_text_pink);
                backgroundColor = getResources().getColor(R.color.color_background_pink);
                break;
            case "purple":
                primaryColor = getResources().getColor(R.color.color_primary_purple);
                lightColor = getResources().getColor(R.color.color_primary_light_purple);
                textColor = getResources().getColor(R.color.color_text_purple);
                backgroundColor = getResources().getColor(R.color.color_background_purple);
                break;
            case "black":
                primaryColor = getResources().getColor(R.color.color_primary_black);
                lightColor = getResources().getColor(R.color.color_primary_light_black);
                textColor = getResources().getColor(R.color.color_text_black);
                backgroundColor = getResources().getColor(R.color.color_background_black);
                break;
            case "white":
            default:
                primaryColor = getResources().getColor(R.color.color_primary_white);
                lightColor = getResources().getColor(R.color.color_primary_light);
                textColor = getResources().getColor(R.color.color_text_white);
                backgroundColor = getResources().getColor(R.color.color_background_white);
                break;
        }

        // Apply colors to preview elements
        if (headerLayout != null) {
            headerLayout.setBackgroundColor(lightColor);
        }

        if (titleTextView != null) {
            titleTextView.setTextColor(textColor);
        }

        if (subtitleTextView != null) {
            subtitleTextView.setTextColor(textColor);
        }

        // Apply background colors to main layout
        if (mainLayout != null) {
            mainLayout.setBackgroundColor(backgroundColor);
        }

        // Change the finish button color
        finishButton.setBackgroundColor(primaryColor);
        finishButton.setTextColor(Color.WHITE);

        // Change radio button text colors dynamically
        updateRadioButtonColors(theme, textColor);
    }

    private void updateRadioButtonColors(String theme, int textColor) {
        // Get all radio buttons and update their text color
        RadioButton[] radioButtons = {
                findViewById(R.id.themeWhite),
                findViewById(R.id.themeGreen),
                findViewById(R.id.themeBlue),
                findViewById(R.id.themePink),
                findViewById(R.id.themePurple),
                findViewById(R.id.themeBlack)
        };

        for (RadioButton rb : radioButtons) {
            if (rb != null) {
                rb.setTextColor(textColor);
            }
        }

        // Also update other text views
        TextView userNameLabel = findViewById(R.id.userNameLabel);
        TextView themeLabel = findViewById(R.id.themeLabel);
        TextView notificationLabel = findViewById(R.id.notificationLabel);
        TextView notificationText = findViewById(R.id.notificationText);

        if (userNameLabel != null) userNameLabel.setTextColor(textColor);
        if (themeLabel != null) themeLabel.setTextColor(textColor);
        if (notificationLabel != null) notificationLabel.setTextColor(textColor);
        if (notificationText != null) notificationText.setTextColor(textColor);

        // Update EditText hint color
        userNameEditText.setHintTextColor(Color.argb(150,
                Color.red(textColor),
                Color.green(textColor),
                Color.blue(textColor)));

        // Update EditText text color
        userNameEditText.setTextColor(textColor);

        // Update debug button text color
        debugDbButton.setTextColor(textColor);
    }

    private void savePreferences() {
        // Get values from UI
        String userName = userNameEditText.getText().toString().trim();
        if (userName.isEmpty()) {
            userName = "User";
        }

        String selectedTheme = getSelectedTheme();
        boolean notifications = notificationSwitch.isChecked();

        Log.d("SETUP", "Attempting to save to Room database: Name=" + userName +
                ", Theme=" + selectedTheme + ", Notifications=" + notifications);

        // Save to Room database
        User user = new User(userName, selectedTheme, notifications);
        userRepository.insertOrUpdate(user);

        // Also save to SharedPreferences for backward compatibility
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("user_name", userName);
        editor.putString("color_theme", selectedTheme);
        editor.putBoolean("notifications", notifications);
        editor.putBoolean(PREF_SETUP_COMPLETED, true);
        editor.apply();

        Toast.makeText(this, "Settings saved to database!", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, getThemeName(selectedTheme) + " theme selected", Toast.LENGTH_SHORT).show();
    }

    private void debugDatabase() {
        StringBuilder debugInfo = new StringBuilder();

        debugInfo.append("📊 ROOM DATABASE DEBUG\n\n");

        // Check user in database
        User user = userRepository.getUserSync();
        if (user != null) {
            debugInfo.append("✅ USER FOUND IN ROOM DATABASE\n");
            debugInfo.append("   • ID: ").append(user.getId()).append("\n");
            debugInfo.append("   • Name: ").append(user.getUserName()).append("\n");
            debugInfo.append("   • Theme: ").append(user.getColorTheme()).append("\n");
            debugInfo.append("   • Notifications: ").append(user.isNotifications() ? "ON" : "OFF").append("\n");
            debugInfo.append("   • Created: ").append(user.getCreatedAt()).append("\n");
        } else {
            debugInfo.append("❌ NO USER IN ROOM DATABASE\n");
        }

        // Check SharedPreferences
        debugInfo.append("\n📝 SHARED PREFERENCES:\n");
        String spName = preferences.getString("user_name", "NOT SET");
        String spTheme = preferences.getString("color_theme", "NOT SET");
        boolean spNotif = preferences.getBoolean("notifications", false);

        debugInfo.append("   • Name: ").append(spName).append("\n");
        debugInfo.append("   • Theme: ").append(spTheme).append("\n");
        debugInfo.append("   • Notifications: ").append(spNotif ? "ON" : "OFF").append("\n");

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Database Debug Info")
                .setMessage(debugInfo.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private String getThemeName(String theme) {
        switch (theme) {
            case "white": return "Light";
            case "green": return "Green";
            case "blue": return "Blue";
            case "pink": return "Pink";
            case "purple": return "Purple";
            case "black": return "Dark";
            default: return "Light";
        }
    }

    private void completeSetup() {
        Toast.makeText(this, "Setup completed!", Toast.LENGTH_SHORT).show();

        // Save the final theme before leaving
        String selectedTheme = getSelectedTheme();

        // Apply theme one more time to ensure it's set
        applyThemePreview(selectedTheme);

        // Also apply theme using ThemeUtils for consistency
        ThemeUtils.applyTheme(this, selectedTheme);

        // Go to main app
        try {
            Intent intent = new Intent(SetupActivity.this, ProductListActivity.class);
            // Pass the selected theme to the next activity
            intent.putExtra("SELECTED_THEME", selectedTheme);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Please create ProductListActivity first", Toast.LENGTH_LONG).show();
        }
    }
}
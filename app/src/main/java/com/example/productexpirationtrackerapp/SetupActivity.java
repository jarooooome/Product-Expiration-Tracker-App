package com.example.productexpirationtrackerapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

public class SetupActivity extends AppCompatActivity {

    private RadioGroup themeGroup;
    private Switch notificationSwitch;
    private EditText userNameEditText;
    private Button finishButton;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        // Initialize views
        themeGroup = findViewById(R.id.themeGroup);
        notificationSwitch = findViewById(R.id.notificationSwitch);
        userNameEditText = findViewById(R.id.userNameEditText);
        finishButton = findViewById(R.id.finishButton);

        // Load saved preferences
        loadSavedPreferences();

        // Finish button
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePreferences();
                completeSetup();
            }
        });
    }

    private void loadSavedPreferences() {
        // Load saved theme - WHITE is default
        String savedTheme = preferences.getString("color_theme", "white");
        setThemeSelection(savedTheme);

        // Load notification preference
        boolean notifications = preferences.getBoolean("notifications", true);
        notificationSwitch.setChecked(notifications);

        // Load user name
        String userName = preferences.getString("user_name", "");
        if (!userName.isEmpty() && !userName.equals("User")) {
            userNameEditText.setText(userName);
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

    private void savePreferences() {
        SharedPreferences.Editor editor = preferences.edit();

        // Save user name
        String userName = userNameEditText.getText().toString().trim();
        if (!userName.isEmpty()) {
            editor.putString("user_name", userName);
        } else {
            editor.putString("user_name", "User");
        }

        // Save color theme
        String selectedTheme = getSelectedTheme();
        editor.putString("color_theme", selectedTheme);

        // Save notification preference
        editor.putBoolean("notifications", notificationSwitch.isChecked());

        // Mark as setup completed
        editor.putBoolean("setup_completed", true);

        editor.apply();

        // Show theme selection toast
        Toast.makeText(this, getThemeName(selectedTheme) + " theme selected", Toast.LENGTH_SHORT).show();
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

        // Go to main app
        try {
            Intent intent = new Intent(SetupActivity.this, ProductListActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Helper method to get theme color
    public static int getThemeColor(String theme) {
        switch (theme) {
            case "white": return 0xFFFFFFFF;
            case "green": return 0xFF4CAF50;
            case "blue": return 0xFF2196F3;
            case "pink": return 0xFFE91E63;
            case "purple": return 0xFF9C27B0;
            case "black": return 0xFF212121;
            default: return 0xFFFFFFFF; // WHITE is default
        }
    }

    // Helper method to get theme light color
    public static int getThemeLightColor(String theme) {
        switch (theme) {
            case "white": return 0xFFF5F5F5;
            case "green": return 0xFFE8F5E9;
            case "blue": return 0xFFE3F2FD;
            case "pink": return 0xFFFCE4EC;
            case "purple": return 0xFFF3E5F5;
            case "black": return 0xFFF5F5F5;
            default: return 0xFFF5F5F5; // WHITE is default
        }
    }
}
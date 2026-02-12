package com.example.productexpirationtrackerapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import android.widget.Button;
import android.view.View;

public class SettingsActivity extends AppCompatActivity {

    private SeekBar reminderFrequencySeekBar;
    private TextView frequencyValueText;
    private SwitchCompat exactAlarmSwitch;
    private Button saveButton;
    private Button cancelButton;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public static final String PREF_REMINDER_DAYS = "reminder_days";
    public static final String PREF_EXACT_ALARM = "exact_alarm_enabled";
    public static final int DEFAULT_REMINDER_DAYS = 3; // Default: 3 days before

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        editor = preferences.edit();

        initializeViews();
        setupSeekBar();
        loadCurrentSettings();
        setupClickListeners();
    }

    private void initializeViews() {
        reminderFrequencySeekBar = findViewById(R.id.reminderFrequencySeekBar);
        frequencyValueText = findViewById(R.id.frequencyValueText);
        exactAlarmSwitch = findViewById(R.id.exactAlarmSwitch);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
    }

    private void setupSeekBar() {
        reminderFrequencySeekBar.setMax(30); // 0-30 days
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

    private void loadCurrentSettings() {
        int currentDays = preferences.getInt(PREF_REMINDER_DAYS, DEFAULT_REMINDER_DAYS);
        reminderFrequencySeekBar.setProgress(currentDays);

        boolean exactAlarmEnabled = preferences.getBoolean(PREF_EXACT_ALARM, true);
        exactAlarmSwitch.setChecked(exactAlarmEnabled);

        // Update display text
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
        boolean exactAlarmEnabled = exactAlarmSwitch.isChecked();

        editor.putInt(PREF_REMINDER_DAYS, reminderDays);
        editor.putBoolean(PREF_EXACT_ALARM, exactAlarmEnabled);
        editor.apply();

        Toast.makeText(this, "Settings saved! Restart app to apply changes.", Toast.LENGTH_LONG).show();
        finish();
    }
}
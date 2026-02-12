package com.example.productexpirationtrackerapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

public class SettingsActivity extends AppCompatActivity {

    private SeekBar reminderFrequencySeekBar;
    private TextView frequencyValueText;
    private RadioGroup vibrationRadioGroup;
    private Button saveButton;
    private Button cancelButton;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public static final String PREF_REMINDER_DAYS = "reminder_days";
    public static final String PREF_VIBRATION_PATTERN = "vibration_pattern";
    public static final int DEFAULT_REMINDER_DAYS = 3;
    public static final String DEFAULT_VIBRATION_PATTERN = "default";

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
        vibrationRadioGroup = findViewById(R.id.vibrationRadioGroup);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
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

        editor.putInt(PREF_REMINDER_DAYS, reminderDays);
        editor.putString(PREF_VIBRATION_PATTERN, vibrationPattern);
        editor.apply();

        // ✅ CRITICAL: Delete and recreate notification channel
        recreateNotificationChannel(vibrationPattern);

        Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * Delete and recreate notification channel with new vibration pattern
     */
    private void recreateNotificationChannel(String vibrationPattern) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);

            // Delete old channel
            manager.deleteNotificationChannel("expiry_channel");
            android.util.Log.d("NOTIF_DEBUG", "🗑️ Deleted notification channel from Settings");

            // Create new channel with updated settings
            NotificationChannel channel = new NotificationChannel(
                    "expiry_channel",
                    "Expiry Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Get notified when products are about to expire");

            // Set vibration pattern
            if (vibrationPattern.equals("none")) {
                channel.enableVibration(false);
                android.util.Log.d("NOTIF_DEBUG", "📳 Settings - Vibration: OFF");
            } else {
                channel.enableVibration(true);
                switch (vibrationPattern) {
                    case "short":
                        channel.setVibrationPattern(new long[]{0, 200, 100, 200});
                        android.util.Log.d("NOTIF_DEBUG", "📳 Settings - Vibration: SHORT");
                        break;
                    case "long":
                        channel.setVibrationPattern(new long[]{0, 800, 200, 800});
                        android.util.Log.d("NOTIF_DEBUG", "📳 Settings - Vibration: LONG");
                        break;
                    case "default":
                    default:
                        channel.setVibrationPattern(new long[]{0, 500, 200, 500});
                        android.util.Log.d("NOTIF_DEBUG", "📳 Settings - Vibration: DEFAULT");
                        break;
                }
            }

            manager.createNotificationChannel(channel);
            android.util.Log.d("NOTIF_DEBUG", "✅ Recreated notification channel with pattern: " + vibrationPattern);
        }
    }
}
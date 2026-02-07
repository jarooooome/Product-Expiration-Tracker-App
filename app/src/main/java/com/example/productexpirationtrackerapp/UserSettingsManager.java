package com.example.productexpirationtrackerapp;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSettingsManager {
    private static final String PREFS_NAME = "AppPrefs";

    public static UserSettings getUserSettings(Context context) {
        UserDatabaseHelper dbHelper = new UserDatabaseHelper(context);
        UserSettings settings = dbHelper.getUserSettings();
        dbHelper.close();

        if (settings == null) {
            // Fall back to SharedPreferences
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            settings = new UserSettings();
            settings.setUserName(prefs.getString("user_name", "User"));
            settings.setColorTheme(prefs.getString("color_theme", "white"));
            settings.setNotifications(prefs.getBoolean("notifications", true));
        }

        return settings;
    }

    public static String getUserName(Context context) {
        UserSettings settings = getUserSettings(context);
        return settings.getUserName();
    }

    public static String getTheme(Context context) {
        UserSettings settings = getUserSettings(context);
        return settings.getColorTheme();
    }

    public static boolean getNotificationsEnabled(Context context) {
        UserSettings settings = getUserSettings(context);
        return settings.isNotifications();
    }
}
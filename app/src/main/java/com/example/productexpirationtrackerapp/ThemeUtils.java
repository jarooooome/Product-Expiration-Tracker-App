package com.example.productexpirationtrackerapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.graphics.Color;

import androidx.core.content.ContextCompat;

public class ThemeUtils {

    private static final String PREFS_NAME = "AppPrefs";
    private static final String PREF_THEME = "color_theme";

    public static String getCurrentTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(PREF_THEME, "light");
    }

    public static void applyTheme(Activity activity) {
        String theme = getCurrentTheme(activity);
        applyTheme(activity, theme);
    }

    public static void applyTheme(Activity activity, String theme) {
        boolean isDarkTheme = theme.equals("dark") || theme.equals("black");

        int primaryColor;
        int lightColor;
        int textColor;
        int fabBackgroundColor;
        int fabIconColor;
        int navBackgroundColor;
        int navInactiveColor;

        if (isDarkTheme) {
            // Dark theme colors
            primaryColor = ContextCompat.getColor(activity, R.color.color_primary_dark);
            lightColor = ContextCompat.getColor(activity, R.color.color_primary_light_black);
            textColor = ContextCompat.getColor(activity, R.color.color_text_dark);
            fabBackgroundColor = ContextCompat.getColor(activity, R.color.color_fab_dark);
            fabIconColor = ContextCompat.getColor(activity, R.color.color_fab_icon_dark);
            navBackgroundColor = ContextCompat.getColor(activity, R.color.color_primary_dark);
            navInactiveColor = Color.parseColor("#757575");
        } else {
            // Light theme colors
            primaryColor = ContextCompat.getColor(activity, R.color.color_primary_light);
            lightColor = ContextCompat.getColor(activity, R.color.color_background_light);
            textColor = ContextCompat.getColor(activity, R.color.color_text_light);
            fabBackgroundColor = ContextCompat.getColor(activity, R.color.color_fab_light);
            fabIconColor = ContextCompat.getColor(activity, R.color.color_fab_icon_light);
            navBackgroundColor = ContextCompat.getColor(activity, R.color.color_primary_light);
            navInactiveColor = Color.parseColor("#757575");
        }

        // Apply background to the main layout
        View rootView = activity.getWindow().getDecorView().getRootView();
        rootView.setBackgroundColor(lightColor);

        // Store theme colors in SharedPreferences for other activities to use
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("fab_background_color", fabBackgroundColor);
        editor.putInt("fab_icon_color", fabIconColor);
        editor.putInt("nav_background_color", navBackgroundColor);
        editor.putInt("nav_inactive_color", navInactiveColor);
        editor.putString(PREF_THEME, theme);
        editor.apply();
    }
}
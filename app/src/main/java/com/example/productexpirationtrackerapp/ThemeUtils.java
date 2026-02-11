package com.example.productexpirationtrackerapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ThemeUtils {

    private static final String PREFS_NAME = "AppPrefs";
    private static final String PREF_THEME = "color_theme";

    public static String getCurrentTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(PREF_THEME, "white");
    }

    public static void applyTheme(Activity activity) {
        String theme = getCurrentTheme(activity);
        applyTheme(activity, theme);
    }

    public static void applyTheme(Activity activity, String theme) {
        int primaryColor;
        int lightColor;
        int textColor;
        int fabBackgroundColor;
        int fabIconColor;
        int navBackgroundColor;
        int navInactiveColor;

        switch (theme) {
            case "green":
                primaryColor = activity.getResources().getColor(R.color.color_primary_green);
                lightColor = activity.getResources().getColor(R.color.color_primary_light_green);
                textColor = activity.getResources().getColor(R.color.color_text_green);
                fabBackgroundColor = activity.getResources().getColor(R.color.color_fab_green);
                fabIconColor = activity.getResources().getColor(R.color.color_fab_icon_green);
                navBackgroundColor = activity.getResources().getColor(R.color.color_nav_background_green);
                navInactiveColor = activity.getResources().getColor(R.color.color_nav_inactive_green);
                break;
            case "blue":
                primaryColor = activity.getResources().getColor(R.color.color_primary_blue);
                lightColor = activity.getResources().getColor(R.color.color_primary_light_blue);
                textColor = activity.getResources().getColor(R.color.color_text_blue);
                fabBackgroundColor = activity.getResources().getColor(R.color.color_fab_blue);
                fabIconColor = activity.getResources().getColor(R.color.color_fab_icon_blue);
                navBackgroundColor = activity.getResources().getColor(R.color.color_nav_background_blue);
                navInactiveColor = activity.getResources().getColor(R.color.color_nav_inactive_blue);
                break;
            case "pink":
                primaryColor = activity.getResources().getColor(R.color.color_primary_pink);
                lightColor = activity.getResources().getColor(R.color.color_primary_light_pink);
                textColor = activity.getResources().getColor(R.color.color_text_pink);
                fabBackgroundColor = activity.getResources().getColor(R.color.color_fab_pink);
                fabIconColor = activity.getResources().getColor(R.color.color_fab_icon_pink);
                navBackgroundColor = activity.getResources().getColor(R.color.color_nav_background_pink);
                navInactiveColor = activity.getResources().getColor(R.color.color_nav_inactive_pink);
                break;
            case "purple":
                primaryColor = activity.getResources().getColor(R.color.color_primary_purple);
                lightColor = activity.getResources().getColor(R.color.color_primary_light_purple);
                textColor = activity.getResources().getColor(R.color.color_text_purple);
                fabBackgroundColor = activity.getResources().getColor(R.color.color_fab_purple);
                fabIconColor = activity.getResources().getColor(R.color.color_fab_icon_purple);
                navBackgroundColor = activity.getResources().getColor(R.color.color_nav_background_purple);
                navInactiveColor = activity.getResources().getColor(R.color.color_nav_inactive_purple);
                break;
            case "black":
                primaryColor = activity.getResources().getColor(R.color.color_primary_black);
                lightColor = activity.getResources().getColor(R.color.color_primary_light_black);
                textColor = activity.getResources().getColor(R.color.color_text_black);
                fabBackgroundColor = activity.getResources().getColor(R.color.color_fab_black);
                fabIconColor = activity.getResources().getColor(R.color.color_fab_icon_black);
                navBackgroundColor = activity.getResources().getColor(R.color.color_nav_background_black);
                navInactiveColor = activity.getResources().getColor(R.color.color_nav_inactive_black);
                break;
            case "white":
            default:
                primaryColor = activity.getResources().getColor(R.color.color_primary_white);
                lightColor = activity.getResources().getColor(R.color.color_primary_light);
                textColor = activity.getResources().getColor(R.color.color_text_white);
                fabBackgroundColor = activity.getResources().getColor(R.color.color_fab_white);
                fabIconColor = activity.getResources().getColor(R.color.color_fab_icon_white);
                navBackgroundColor = activity.getResources().getColor(R.color.color_nav_background_white);
                navInactiveColor = activity.getResources().getColor(R.color.color_nav_inactive_white);
                break;
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
        editor.apply();
    }
}
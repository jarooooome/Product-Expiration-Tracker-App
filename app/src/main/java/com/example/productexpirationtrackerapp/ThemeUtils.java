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

        switch (theme) {
            case "green":
                primaryColor = activity.getResources().getColor(R.color.color_primary_green);
                lightColor = activity.getResources().getColor(R.color.color_primary_light_green);
                textColor = activity.getResources().getColor(R.color.color_text_green);
                break;
            case "blue":
                primaryColor = activity.getResources().getColor(R.color.color_primary_blue);
                lightColor = activity.getResources().getColor(R.color.color_primary_light_blue);
                textColor = activity.getResources().getColor(R.color.color_text_blue);
                break;
            case "pink":
                primaryColor = activity.getResources().getColor(R.color.color_primary_pink);
                lightColor = activity.getResources().getColor(R.color.color_primary_light_pink);
                textColor = activity.getResources().getColor(R.color.color_text_pink);
                break;
            case "purple":
                primaryColor = activity.getResources().getColor(R.color.color_primary_purple);
                lightColor = activity.getResources().getColor(R.color.color_primary_light_purple);
                textColor = activity.getResources().getColor(R.color.color_text_purple);
                break;
            case "black":
                primaryColor = activity.getResources().getColor(R.color.color_primary_black);
                lightColor = activity.getResources().getColor(R.color.color_primary_light_black);
                textColor = activity.getResources().getColor(R.color.color_text_black);
                break;
            case "white":
            default:
                primaryColor = activity.getResources().getColor(R.color.color_primary_white);
                lightColor = activity.getResources().getColor(R.color.color_primary_light);
                textColor = activity.getResources().getColor(R.color.color_text_white);
                break;
        }

        // Apply background to the main layout
        View rootView = activity.getWindow().getDecorView().getRootView();
        rootView.setBackgroundColor(lightColor);

        // You can add more theme application logic here
        // For example, style all buttons, text views, etc.
    }
}
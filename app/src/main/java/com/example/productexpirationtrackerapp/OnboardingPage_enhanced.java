package com.example.productexpirationtrackerapp;

public class OnboardingPage_enhanced {
    private final String title;
    private final String description;
    private final String icon; // Emoji fallback
    private final int iconResId; // Vector drawable resource ID
    private final int backgroundResId; // Gradient background resource ID

    // Constructor with vector drawable
    public OnboardingPage_enhanced(String title, String description, int iconResId, int backgroundResId) {
        this.title = title;
        this.description = description;
        this.iconResId = iconResId;
        this.backgroundResId = backgroundResId;
        this.icon = ""; // No emoji
    }

    // Fallback constructor with emoji (backward compatible)
    public OnboardingPage_enhanced(String title, String description, String icon) {
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.iconResId = 0; // No vector drawable
        this.backgroundResId = 0; // Use default gradient
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public int getIconResId() {
        return iconResId;
    }

    public int getBackgroundResId() {
        return backgroundResId;
    }

    public boolean hasVectorIcon() {
        return iconResId != 0;
    }

    public boolean hasCustomBackground() {
        return backgroundResId != 0;
    }
}
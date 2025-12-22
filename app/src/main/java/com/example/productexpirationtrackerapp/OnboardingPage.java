package com.example.productexpirationtrackerapp;

public class OnboardingPage {
    private String title;
    private String description;
    private String icon;

    public OnboardingPage(String title, String description, String icon) {
        this.title = title;
        this.description = description;
        this.icon = icon;
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
}
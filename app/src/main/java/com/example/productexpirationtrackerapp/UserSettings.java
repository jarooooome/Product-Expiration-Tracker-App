package com.example.productexpirationtrackerapp;

public class UserSettings {
    private int id;
    private String userName;
    private String colorTheme;
    private boolean notifications;

    // Constructors
    public UserSettings() {}

    public UserSettings(String userName, String colorTheme, boolean notifications) {
        this.userName = userName;
        this.colorTheme = colorTheme;
        this.notifications = notifications;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getColorTheme() { return colorTheme; }
    public void setColorTheme(String colorTheme) { this.colorTheme = colorTheme; }

    public boolean isNotifications() { return notifications; }
    public void setNotifications(boolean notifications) { this.notifications = notifications; }

    @Override
    public String toString() {
        return "UserSettings{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", colorTheme='" + colorTheme + '\'' +
                ", notifications=" + notifications +
                '}';
    }
}
package com.example.productexpirationtrackerapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;        // Changed from userName
    private String email;       // Added
    private String password;    // Added
    private String colorTheme;
    private boolean notifications;
    private Date createdAt;

    public User() {
        this.createdAt = new Date();
    }

    public User(String name, String email, String password, String colorTheme, boolean notifications) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.colorTheme = colorTheme;
        this.notifications = notifications;
        this.createdAt = new Date();
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getColorTheme() { return colorTheme; }
    public void setColorTheme(String colorTheme) { this.colorTheme = colorTheme; }

    public boolean isNotifications() { return notifications; }
    public void setNotifications(boolean notifications) { this.notifications = notifications; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
package com.example.productexpirationtrackerapp;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {
    private UserDao userDao;
    private ExecutorService executorService;
    private Handler mainHandler;
    private static final String TAG = "UserRepository";

    public UserRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        userDao = database.userDao();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public interface RepositoryCallback {
        void onComplete(boolean success);
    }

    public void insertOrUpdate(User user, RepositoryCallback callback) {
        Log.d("REPO", "🔵 insertOrUpdate called with user theme: " + user.getColorTheme());

        executorService.execute(() -> {
            boolean success = false;
            try {
                Log.d("REPO", "🟡 Executing in background thread");

                // Check if user exists
                User existingUser = userDao.getUser();
                Log.d("REPO", "Existing user: " + (existingUser != null ? existingUser.getColorTheme() : "null"));

                if (existingUser == null) {
                    // Insert new user
                    long id = userDao.insert(user);
                    user.setId((int) id);
                    Log.d("REPO", "✅ New user inserted with ID: " + id + ", Theme: " + user.getColorTheme());
                    success = true;
                } else {
                    // Update existing user
                    user.setId(existingUser.getId());
                    userDao.update(user);
                    Log.d("REPO", "✅ User updated successfully, Theme: " + user.getColorTheme());
                    success = true;
                }

                // Verify after operation
                User afterOp = userDao.getUser();
                Log.d("REPO", "🟢 After operation - User in DB: " + (afterOp != null ? afterOp.getColorTheme() : "null"));

            } catch (Exception e) {
                Log.e("REPO", "❌ Error in insertOrUpdate: " + e.getMessage());
                e.printStackTrace();
                success = false;
            }

            final boolean finalSuccess = success;
            mainHandler.post(() -> {
                callback.onComplete(finalSuccess);
            });
        });
    }

    // Keep old method for backward compatibility
    public void insertOrUpdate(User user) {
        insertOrUpdate(user, success -> {
            Log.d("REPO", "Insert/Update completed with success: " + success);
        });
    }

    // ✅ ADD THIS METHOD - Para i-update lang ang theme
    public void updateTheme(String theme) {
        executorService.execute(() -> {
            try {
                User user = userDao.getUser();
                if (user != null) {
                    user.setColorTheme(theme);
                    userDao.update(user);
                    Log.d(TAG, "Theme updated to: " + theme);
                } else {
                    Log.e(TAG, "No user found to update theme");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating theme: " + e.getMessage());
            }
        });
    }

    // ✅ FIXED: Get user with callback
    public void getUser(UserRepositoryCallback callback) {
        executorService.execute(() -> {
            try {
                User user = userDao.getUser();
                mainHandler.post(() -> {
                    callback.onUserLoaded(user);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error getting user: " + e.getMessage());
                mainHandler.post(() -> {
                    callback.onUserLoaded(null);
                });
            }
        });
    }

    // Keep existing getUserSync for backward compatibility
    public User getUserSync() {
        try {
            return userDao.getUser();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Callback interface
    public interface UserRepositoryCallback {
        void onUserLoaded(User user);
    }
}
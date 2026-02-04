package com.example.productexpirationtrackerapp;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {
    private UserDao userDao;
    private ExecutorService executorService;

    public UserRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        userDao = database.userDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insertOrUpdate(User user) {
        executorService.execute(() -> {
            // Check if user exists
            User existingUser = userDao.getUser();
            if (existingUser == null) {
                // Insert new user
                userDao.insert(user);
            } else {
                // Update existing user
                user.setId(existingUser.getId());
                userDao.update(user);
            }
        });
    }

    public LiveData<User> getUser() {
        // You need to convert to LiveData in DAO first
        return null; // We'll implement this if needed
    }

    public User getUserSync() {
        try {
            return new GetUserAsync(userDao).execute().get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class GetUserAsync extends AsyncTask<Void, Void, User> {
        private UserDao userDao;

        GetUserAsync(UserDao userDao) {
            this.userDao = userDao;
        }

        @Override
        protected User doInBackground(Void... voids) {
            return userDao.getUser();
        }
    }
}
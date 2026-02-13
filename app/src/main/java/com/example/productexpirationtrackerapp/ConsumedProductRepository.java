package com.example.productexpirationtrackerapp;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsumedProductRepository {
    private ConsumedProductDao consumedProductDao;
    private ExecutorService executorService;
    private Handler mainHandler;
    private static final String TAG = "ConsumedRepo";

    public ConsumedProductRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        consumedProductDao = database.consumedProductDao();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    // Insert a consumed product
    public void insert(ConsumedProduct consumedProduct) {
        executorService.execute(() -> {
            try {
                consumedProductDao.insert(consumedProduct);
                Log.d(TAG, "✅ Consumed product inserted: " + consumedProduct.getProductName());
            } catch (Exception e) {
                Log.e(TAG, "❌ Error inserting consumed product: " + e.getMessage());
            }
        });
    }

    // Get all consumed products (LiveData for automatic UI updates)
    public LiveData<List<ConsumedProduct>> getAllConsumedProducts() {
        return consumedProductDao.getAllConsumedProducts();
    }

    // Get consumed products by type (CONSUMED or DISCARDED)
    public LiveData<List<ConsumedProduct>> getConsumedProductsByType(String actionType) {
        return consumedProductDao.getConsumedProductsByType(actionType);
    }

    // Get count of all consumed products
    public void getCount(RepositoryCallback<Integer> callback) {
        executorService.execute(() -> {
            try {
                int count = consumedProductDao.getCount();
                mainHandler.post(() -> callback.onSuccess(count));
            } catch (Exception e) {
                Log.e(TAG, "Error getting count: " + e.getMessage());
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    // Get count by type
    public void getCountByType(String actionType, RepositoryCallback<Integer> callback) {
        executorService.execute(() -> {
            try {
                int count = consumedProductDao.getCountByType(actionType);
                mainHandler.post(() -> callback.onSuccess(count));
            } catch (Exception e) {
                Log.e(TAG, "Error getting count by type: " + e.getMessage());
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    // Delete all consumed products
    public void deleteAll() {
        executorService.execute(() -> {
            try {
                consumedProductDao.deleteAll();
                Log.d(TAG, "✅ All consumed products deleted");
            } catch (Exception e) {
                Log.e(TAG, "❌ Error deleting all: " + e.getMessage());
            }
        });
    }

    // Callback interface for async operations
    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}
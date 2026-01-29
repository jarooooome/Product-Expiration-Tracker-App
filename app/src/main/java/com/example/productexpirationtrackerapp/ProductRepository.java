package com.example.productexpirationtrackerapp;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductRepository {

    private ProductDao productDao;
    private LiveData<List<Product>> allProducts;
    private ExecutorService executorService;

    public ProductRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        productDao = database.productDao();
        allProducts = productDao.getAllProductsLiveData();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Product>> getAllProducts() {
        return allProducts;
    }

    public void insert(Product product) {
        executorService.execute(() -> {
            productDao.insert(product);
        });
    }

    public void update(Product product) {
        executorService.execute(() -> {
            productDao.update(product);
        });
    }

    public void delete(Product product) {
        executorService.execute(() -> {
            productDao.delete(product);
        });
    }

    public void deleteById(int productId) {
        executorService.execute(() -> {
            productDao.deleteById(productId);
        });
    }

    public void findProductById(int productId, MutableLiveData<List<Product>> searchResults) {
        executorService.execute(() -> {
            Product product = productDao.getProductById(productId);
            if (product != null) {
                searchResults.postValue(List.of(product));
            } else {
                searchResults.postValue(List.of());
            }
        });
    }
}
package com.example.productexpirationtrackerapp;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductRepository {
    private final ProductDao productDao;
    private final LiveData<List<Product>> allProductsLiveData;
    private final ExecutorService executorService;

    public ProductRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        productDao = database.productDao();
        allProductsLiveData = productDao.getAllProductsLiveData();
        executorService = Executors.newSingleThreadExecutor();
    }

    // LiveData version for ViewModel
    public LiveData<List<Product>> getAllProducts() {
        return allProductsLiveData;
    }

    // Non-LiveData version for synchronous operations
    public List<Product> getAllProductsSync() {
        return productDao.getAllProducts();
    }

    public LiveData<List<Product>> getProductsByCategory(String category) {
        return productDao.getProductsByCategoryLiveData(category);
    }

    public List<Product> getProductsByCategorySync(String category) {
        return productDao.getProductsByCategory(category);
    }

    public void insert(Product product) {
        executorService.execute(() -> productDao.insert(product));
    }

    public void update(Product product) {
        executorService.execute(() -> productDao.update(product));
    }

    public void delete(Product product) {
        executorService.execute(() -> productDao.delete(product));
    }

    public void deleteAll() {
        executorService.execute(() -> productDao.deleteAll());
    }

    public Product getProductById(int id) {
        return productDao.getProductById(id);
    }

    // Fix this method - you need to pass a MutableLiveData to update
    public void findProductById(int id, MutableLiveData<List<Product>> searchResults) {
        executorService.execute(() -> {
            Product product = productDao.getProductById(id);
            if (product != null) {
                List<Product> resultList = new java.util.ArrayList<>();
                resultList.add(product);
                searchResults.postValue(resultList);
            } else {
                searchResults.postValue(new java.util.ArrayList<>());
            }
        });
    }

    public void deleteById(int id) {
        executorService.execute(() -> {
            Product product = productDao.getProductById(id);
            if (product != null) {
                productDao.delete(product);
            }
        });
    }
}
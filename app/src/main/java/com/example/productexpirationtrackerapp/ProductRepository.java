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
    private NotificationScheduler notificationScheduler; // Moved to top with other fields

    public ProductRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        productDao = database.productDao();
        allProducts = productDao.getAllProductsLiveData();
        executorService = Executors.newSingleThreadExecutor();
        notificationScheduler = new NotificationScheduler(application); // Initialize here
    }

    public LiveData<List<Product>> getAllProducts() {
        return allProducts;
    }

    // SINGLE insert method - with notification scheduling
    public void insert(Product product) {
        executorService.execute(() -> {
            long id = productDao.insert(product);
            product.setId((int) id); // Set the generated ID

            // Schedule notifications for this product
            if (notificationScheduler != null) {
                notificationScheduler.onProductAdded(product);
            }
        });
    }

    // SINGLE update method
    public void update(Product product) {
        executorService.execute(() -> {
            productDao.update(product);
            // Optionally reschedule notifications after update
            if (notificationScheduler != null) {
                notificationScheduler.onProductEdited(product);
            }
        });
    }

    // SINGLE delete method - with notification cancellation
    public void delete(Product product) {
        executorService.execute(() -> {
            // Cancel scheduled notifications first
            if (notificationScheduler != null) {
                notificationScheduler.onProductRemoved(product.getId());
            }
            productDao.delete(product);
        });
    }

    public void deleteById(int productId) {
        executorService.execute(() -> {
            // First get the product to cancel its notifications
            Product product = productDao.getProductById(productId);
            if (product != null && notificationScheduler != null) {
                notificationScheduler.onProductRemoved(product.getId());
            }
            productDao.deleteById(productId);
        });
    }

    // SINGLE rescheduleAllNotifications method
    public void rescheduleAllNotifications() {
        executorService.execute(() -> {
            List<Product> products = productDao.getAllProducts();
            if (notificationScheduler != null) {
                notificationScheduler.rescheduleAll(products);
            }
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

    // Category filtering method
    public LiveData<List<Product>> getProductsByCategory(String category) {
        return productDao.getProductsByCategory(category);
    }
}
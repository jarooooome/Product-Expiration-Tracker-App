package com.example.productexpirationtrackerapp;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;

public class ProductViewModel extends AndroidViewModel {

    private ProductRepository productRepository;
    private LiveData<List<Product>> allProducts;
    private MutableLiveData<List<Product>> searchResults;

    public ProductViewModel(Application application) {
        super(application);
        productRepository = new ProductRepository(application);
        allProducts = productRepository.getAllProducts();
        searchResults = new MutableLiveData<>();
    }

    public LiveData<List<Product>> getAllProducts() {
        return allProducts;
    }

    public MutableLiveData<List<Product>> getSearchResults() {
        return searchResults;
    }

    public void insert(Product product) {
        productRepository.insert(product);
    }

    public void delete(Product product) {
        productRepository.delete(product);
    }

    public void update(Product product) {
        productRepository.update(product);
    }

    public void deleteById(int productId) {
        productRepository.deleteById(productId);
    }

    public void findProductById(int productId) {
        productRepository.findProductById(productId, searchResults);
    }

    // NEW: Add category filtering method
    public LiveData<List<Product>> getProductsByCategory(String category) {
        return productRepository.getProductsByCategory(category);
    }
}
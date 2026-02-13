package com.example.productexpirationtrackerapp;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class ConsumedProductViewModel extends AndroidViewModel {

    private ConsumedProductRepository repository;
    private LiveData<List<ConsumedProduct>> allConsumedProducts;
    private LiveData<List<ConsumedProduct>> consumedProducts;
    private LiveData<List<ConsumedProduct>> discardedProducts;

    public ConsumedProductViewModel(Application application) {
        super(application);
        repository = new ConsumedProductRepository(application);
        allConsumedProducts = repository.getAllConsumedProducts();
        consumedProducts = repository.getConsumedProductsByType("CONSUMED");
        discardedProducts = repository.getConsumedProductsByType("DISCARDED");
    }

    // Get all consumed products
    public LiveData<List<ConsumedProduct>> getAllConsumedProducts() {
        return allConsumedProducts;
    }

    // Get only CONSUMED products
    public LiveData<List<ConsumedProduct>> getConsumedProducts() {
        return consumedProducts;
    }

    // Get only DISCARDED products
    public LiveData<List<ConsumedProduct>> getDiscardedProducts() {
        return discardedProducts;
    }

    // Insert a consumed product
    public void insert(ConsumedProduct consumedProduct) {
        repository.insert(consumedProduct);
    }

    // Delete all
    public void deleteAll() {
        repository.deleteAll();
    }

    // Get count (with callback)
    public void getCount(ConsumedProductRepository.RepositoryCallback<Integer> callback) {
        repository.getCount(callback);
    }

    // Get count by type
    public void getCountByType(String actionType, ConsumedProductRepository.RepositoryCallback<Integer> callback) {
        repository.getCountByType(actionType, callback);
    }
}
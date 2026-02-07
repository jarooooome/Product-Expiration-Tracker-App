package com.example.productexpirationtrackerapp;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ProductDao {
    @Insert
    void insert(Product product);

    @Update
    void update(Product product);

    @Delete
    void delete(Product product);

    // CHANGED: Added deleteById method that ProductRepository expects
    @Query("DELETE FROM products WHERE id = :productId")
    void deleteById(int productId);

    // CHANGED: Renamed to getAllProductsLiveData and returns LiveData
    @Query("SELECT * FROM products ORDER BY expiryDate ASC")
    LiveData<List<Product>> getAllProductsLiveData();

    // KEPT: Original method (you can keep both if needed)
    @Query("SELECT * FROM products ORDER BY expiryDate ASC")
    List<Product> getAllProducts();

    @Query("SELECT * FROM products WHERE id = :productId")
    Product getProductById(int productId);

    @Query("SELECT * FROM products WHERE expiryDate <= date('now', '+7 days') ORDER BY expiryDate ASC")
    List<Product> getExpiringSoonProducts();

    // CHANGED: Returns LiveData for ProductRepository compatibility
    @Query("SELECT * FROM products WHERE category = :category ORDER BY expiryDate ASC")
    LiveData<List<Product>> getProductsByCategory(String category);

    @Query("DELETE FROM products")
    void deleteAll();

    // ADDITIONAL: Optional method for LiveData version of expiring soon
    @Query("SELECT * FROM products WHERE expiryDate <= date('now', '+7 days') ORDER BY expiryDate ASC")
    LiveData<List<Product>> getExpiringSoonProductsLiveData();
}
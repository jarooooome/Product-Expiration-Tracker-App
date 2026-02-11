package com.example.productexpirationtrackerapp;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;
import java.util.List;
import java.util.Date;

@Dao
public interface ProductDao {
    @Insert
    void insert(Product product);

    @Update
    void update(Product product);

    @Delete
    void delete(Product product);

    @Query("DELETE FROM products WHERE id = :productId")
    void deleteById(int productId);

    // ADD THIS METHOD - it's missing from your current file
    @Query("SELECT * FROM products ORDER BY expiryDate ASC")
    LiveData<List<Product>> getAllProductsLiveData();

    // Keep the non-LiveData version if needed elsewhere
    @Query("SELECT * FROM products ORDER BY expiryDate ASC")
    List<Product> getAllProducts();

    @Query("SELECT * FROM products WHERE id = :productId")
    Product getProductById(int productId);

    @Query("SELECT * FROM products WHERE expiryDate < :currentDate")
    List<Product> getExpiredProducts(Date currentDate);

    @Query("SELECT * FROM products WHERE expiryDate BETWEEN :startDate AND :endDate")
    List<Product> getProductsExpiringBetween(Date startDate, Date endDate);

    @Query("SELECT * FROM products WHERE category = :category ORDER BY expiryDate ASC")
    LiveData<List<Product>> getProductsByCategory(String category);
}
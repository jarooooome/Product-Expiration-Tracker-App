package com.example.productexpirationtrackerapp;

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

    @Query("SELECT * FROM products ORDER BY expiryDate ASC")
    List<Product> getAllProducts();

    @Query("SELECT * FROM products WHERE id = :productId")
    Product getProductById(int productId);

    @Query("SELECT * FROM products WHERE expiryDate <= date('now', '+7 days') ORDER BY expiryDate ASC")
    List<Product> getExpiringSoonProducts();

    @Query("SELECT * FROM products WHERE category = :category ORDER BY expiryDate ASC")
    List<Product> getProductsByCategory(String category);

    @Query("DELETE FROM products")
    void deleteAll();
}
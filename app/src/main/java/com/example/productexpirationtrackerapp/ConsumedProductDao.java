package com.example.productexpirationtrackerapp;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ConsumedProductDao {

    @Insert
    void insert(ConsumedProduct consumedProduct);

    @Query("SELECT * FROM consumed_products ORDER BY actionDate DESC")
    LiveData<List<ConsumedProduct>> getAllConsumedProducts();

    @Query("SELECT * FROM consumed_products WHERE actionType = :actionType ORDER BY actionDate DESC")
    LiveData<List<ConsumedProduct>> getConsumedProductsByType(String actionType);

    @Query("SELECT * FROM consumed_products WHERE originalProductId = :productId")
    ConsumedProduct getConsumedProductByOriginalId(int productId);

    @Query("DELETE FROM consumed_products WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM consumed_products")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM consumed_products")
    int getCount();

    @Query("SELECT COUNT(*) FROM consumed_products WHERE actionType = :actionType")
    int getCountByType(String actionType);
}
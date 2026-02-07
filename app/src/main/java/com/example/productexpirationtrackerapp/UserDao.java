package com.example.productexpirationtrackerapp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface UserDao {
    @Insert
    long insert(User user);

    @Update
    void update(User user);

    @Query("SELECT * FROM users LIMIT 1")
    User getUser();

    @Query("SELECT COUNT(*) FROM users")
    int getUserCount();

    @Query("DELETE FROM users")
    void deleteAllUsers();
}
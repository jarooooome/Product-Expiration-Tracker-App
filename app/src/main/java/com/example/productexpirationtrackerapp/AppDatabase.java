package com.example.productexpirationtrackerapp;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;

@Database(entities = {Product.class, User.class, ConsumedProduct.class}, version = 3, exportSchema = false)  // Version increased to 3
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract ProductDao productDao();
    public abstract UserDao userDao();
    public abstract ConsumedProductDao consumedProductDao();  // ADDED

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "app_database"
                            ).fallbackToDestructiveMigration()  // This will delete old data on version change
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
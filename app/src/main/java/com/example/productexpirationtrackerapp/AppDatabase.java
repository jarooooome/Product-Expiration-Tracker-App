package com.example.productexpirationtrackerapp;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;

@Database(entities = {Product.class, User.class}, version = 2, exportSchema = false)  // Version increased to 2
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract ProductDao productDao();
    public abstract UserDao userDao();  // Added

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "app_database"  // Changed name
                            ).fallbackToDestructiveMigration()  // This will delete old data on version change
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
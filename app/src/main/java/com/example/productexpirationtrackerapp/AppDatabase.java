package com.example.productexpirationtrackerapp;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;

@Database(entities = {Product.class, User.class}, version = 3, exportSchema = false) // ← Added User.class, increased version to 3
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract ProductDao productDao();
    public abstract UserDao userDao();

    private static volatile AppDatabase INSTANCE;

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE products ADD COLUMN brand TEXT");
            database.execSQL("ALTER TABLE products ADD COLUMN category TEXT");
            database.execSQL("ALTER TABLE products ADD COLUMN quantity INTEGER DEFAULT 1");
            database.execSQL("ALTER TABLE products ADD COLUMN storageLocation TEXT");
            database.execSQL("ALTER TABLE products ADD COLUMN notes TEXT");
            database.execSQL("ALTER TABLE products ADD COLUMN photoPath TEXT");
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Create users table for the new User entity
            database.execSQL("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "name TEXT, " +
                    "email TEXT, " +
                    "password TEXT" +
                    ")");
        }
    };

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "product_database")
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // ← Added both migrations
                            .fallbackToDestructiveMigration() // ← Optional: for development
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
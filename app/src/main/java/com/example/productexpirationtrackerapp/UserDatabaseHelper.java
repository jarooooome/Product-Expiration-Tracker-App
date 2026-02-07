package com.example.productexpirationtrackerapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class UserDatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "UserSettings.db";
    private static final int DATABASE_VERSION = 1;

    // Table Name
    private static final String TABLE_USERS = "users";

    // Column Names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USER_NAME = "user_name";
    private static final String COLUMN_COLOR_THEME = "color_theme";
    private static final String COLUMN_NOTIFICATIONS = "notifications";
    private static final String COLUMN_CREATED_AT = "created_at";

    // Create Table SQL
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USER_NAME + " TEXT,"
            + COLUMN_COLOR_THEME + " TEXT DEFAULT 'white',"
            + COLUMN_NOTIFICATIONS + " INTEGER DEFAULT 1,"  // 1 = true, 0 = false
            + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
            + ")";

    public UserDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DATABASE", "Creating table: " + CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_USERS);
        Log.d("DATABASE", "Table 'users' created successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DATABASE", "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Add or Update User Settings
    public long addOrUpdateUser(String userName, String colorTheme, boolean notifications) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            Log.d("DATABASE", "Database opened for writing");

            // First, check if user exists
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS, null);
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();
            Log.d("DATABASE", "Current row count: " + count);

            ContentValues values = new ContentValues();
            values.put(COLUMN_USER_NAME, userName);
            values.put(COLUMN_COLOR_THEME, colorTheme);
            values.put(COLUMN_NOTIFICATIONS, notifications ? 1 : 0);

            long result;
            if (count == 0) {
                // Insert new user
                Log.d("DATABASE", "Inserting new user: " + userName);
                result = db.insert(TABLE_USERS, null, values);
                Log.d("DATABASE", "Insert result (row ID): " + result);
            } else {
                // Update existing user
                Log.d("DATABASE", "Updating existing user");
                result = db.update(TABLE_USERS, values, COLUMN_ID + " = ?", new String[]{"1"});
                Log.d("DATABASE", "Update result (rows affected): " + result);
            }

            return result;
        } catch (Exception e) {
            Log.e("DATABASE", "Error saving user: " + e.getMessage());
            e.printStackTrace();
            return -1;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
                Log.d("DATABASE", "Database closed");
            }
        }
    }

    // Get User Settings
    public UserSettings getUserSettings() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            Log.d("DATABASE", "Database opened for reading");

            UserSettings settings = null;

            cursor = db.query(TABLE_USERS,
                    new String[]{COLUMN_ID, COLUMN_USER_NAME, COLUMN_COLOR_THEME, COLUMN_NOTIFICATIONS},
                    null, null, null, null, COLUMN_ID + " DESC", "1");

            Log.d("DATABASE", "Query executed. Cursor count: " + (cursor != null ? cursor.getCount() : 0));

            if (cursor != null && cursor.moveToFirst()) {
                settings = new UserSettings();
                settings.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                settings.setUserName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)));
                settings.setColorTheme(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COLOR_THEME)));
                settings.setNotifications(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATIONS)) == 1);

                Log.d("DATABASE", "Retrieved: " + settings.toString());
            } else {
                Log.d("DATABASE", "No data found in users table");
            }

            return settings;
        } catch (Exception e) {
            Log.e("DATABASE", "Error getting user: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
                Log.d("DATABASE", "Database closed");
            }
        }
    }

    // Check if user exists
    public boolean hasUserSettings() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        db.close();
        Log.d("DATABASE", "hasUserSettings: " + (count > 0));
        return count > 0;
    }

    // Get all rows (for debugging)
    public void printAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS, null);

        Log.d("DATABASE", "=== PRINTING ALL USERS ===");
        Log.d("DATABASE", "Total rows: " + cursor.getCount());

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME));
            String theme = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COLOR_THEME));
            int notif = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATIONS));

            Log.d("DATABASE", "Row: ID=" + id + ", Name=" + name + ", Theme=" + theme + ", Notif=" + notif);
        }

        cursor.close();
        db.close();
    }
}
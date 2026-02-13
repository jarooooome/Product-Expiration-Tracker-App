package com.example.productexpirationtrackerapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.ExistingWorkPolicy;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NotificationScheduler {

    private Context context;
    private WorkManager workManager;
    private static final String TAG = "NOTIF_DEBUG";

    // TEST MODE FLAG - Set to false for production!
    private static final boolean TEST_MODE = true; // CHANGED TO FALSE FOR PRODUCTION

    public NotificationScheduler(Context context) {
        this.context = context;
        this.workManager = WorkManager.getInstance(context);
        Log.d(TAG, "===== NOTIFICATION SCHEDULER INITIALIZED =====");
        Log.d(TAG, "Android version: " + Build.VERSION.SDK_INT);
        Log.d(TAG, "TEST MODE: " + (TEST_MODE ? "ENABLED - 1 minute notifications" : "DISABLED"));
        Log.d(TAG, "✅ Using WorkManager for reliable background execution");
    }

    /**
     * Get user's preferred reminder days from SharedPreferences
     */
    private int getPreferredReminderDays() {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return prefs.getInt("reminder_days", 3);
    }

    /**
     * Get user's preferred notification hour from SharedPreferences
     */
    private int getNotificationHour() {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return prefs.getInt("notification_hour", 9); // Default: 9 AM
    }

    /**
     * Get user's preferred notification minute from SharedPreferences
     */
    private int getNotificationMinute() {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return prefs.getInt("notification_minute", 0); // Default: 0
    }

    /**
     * Schedule notification for a product using WorkManager
     * @param daysBefore How many days before expiry to notify
     */
    public void scheduleExpiryNotification(Product product, int daysBefore) {
        try {
            Log.d(TAG, "===== SCHEDULING NOTIFICATION =====");
            Log.d(TAG, "Product: " + product.getName() + " (ID: " + product.getId() + ")");
            Log.d(TAG, "Expiry date: " + product.getExpiryDate());
            Log.d(TAG, "Days before: " + daysBefore);

            Date expiryDate = product.getExpiryDate();
            if (expiryDate == null) {
                Log.e(TAG, "❌ Expiry date is null!");
                return;
            }

            // Calculate notification time
            Calendar notificationCalendar = Calendar.getInstance();

            if (TEST_MODE) {
                // TEST MODE: Schedule for 1 minute from now
                notificationCalendar = Calendar.getInstance();
                notificationCalendar.add(Calendar.MINUTE, 1);
                notificationCalendar.set(Calendar.SECOND, 0);
                notificationCalendar.set(Calendar.MILLISECOND, 0);
                Log.d(TAG, "🔴 TEST MODE ACTIVE - Scheduling for: " + notificationCalendar.getTime());
            } else {
                // NORMAL MODE: Calculate notification time based on expiry date
                notificationCalendar.setTime(expiryDate);
                notificationCalendar.add(Calendar.DAY_OF_YEAR, -daysBefore);

                // ✅ USE USER'S SELECTED TIME FROM SETTINGS
                notificationCalendar.set(Calendar.HOUR_OF_DAY, getNotificationHour());
                notificationCalendar.set(Calendar.MINUTE, getNotificationMinute());
                notificationCalendar.set(Calendar.SECOND, 0);
                notificationCalendar.set(Calendar.MILLISECOND, 0);

                Log.d(TAG, "Scheduled time: " + notificationCalendar.getTime());
                Log.d(TAG, "Notification time: " + String.format("%02d:%02d",
                        getNotificationHour(), getNotificationMinute()));
            }

            // Calculate delay
            long now = System.currentTimeMillis();
            long scheduledTime = notificationCalendar.getTimeInMillis();
            long delayMillis = scheduledTime - now;

            Log.d(TAG, "Current time: " + Calendar.getInstance().getTime());
            Log.d(TAG, "Delay: " + delayMillis/1000 + " seconds");

            // If the notification time is already passed, don't schedule
            if (delayMillis <= 0) {
                Log.d(TAG, "❌ Notification time already passed for: " + product.getName());
                Log.d(TAG, "Time difference: " + delayMillis + "ms");
                return;
            }

            // Check if product ID is valid
            if (product.getId() <= 0) {
                Log.e(TAG, "❌ Invalid product ID: " + product.getId());
                return;
            }

            // Create unique work ID for this product and reminder day
            String uniqueWorkName = "expiry_" + product.getId() + "_" + daysBefore;

            // Prepare input data for the worker
            Data inputData = new Data.Builder()
                    .putInt("product_id", product.getId())
                    .putString("product_name", product.getName())
                    .putInt("days_left", daysBefore)
                    .build();

            // Create OneTimeWorkRequest
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                    .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .addTag(uniqueWorkName)
                    .addTag("product_" + product.getId())
                    .build();

            // Enqueue the work request with unique name (prevents duplicates)
            workManager.enqueueUniqueWork(
                    uniqueWorkName,
                    ExistingWorkPolicy.REPLACE, // Replace existing work with same name
                    workRequest
            );

            Log.d(TAG, "✅✅✅ WORK SCHEDULED with WorkManager");
            Log.d(TAG, "   • Work ID: " + uniqueWorkName);
            Log.d(TAG, "   • Product: " + product.getName());
            Log.d(TAG, "   • Time: " + notificationCalendar.getTime());
            Log.d(TAG, "   • Delay: " + delayMillis/1000 + " seconds");
            Log.d(TAG, "   • ✅ Survives app force close");
            Log.d(TAG, "   • ✅ Survives device reboot");
            Log.d(TAG, "   • ✅ No permissions needed");
            Log.d(TAG, "   • ✅ Battery efficient");
            Log.d(TAG, "===== SCHEDULING COMPLETE =====\n");

        } catch (Exception e) {
            Log.e(TAG, "❌ ERROR scheduling notification", e);
        }
    }

    /**
     * Schedule all notifications for a product
     */
    public void scheduleAllNotifications(Product product) {
        Log.d(TAG, "===== SCHEDULING ALL NOTIFICATIONS FOR: " + product.getName() + " =====");

        if (TEST_MODE) {
            Log.d(TAG, "🔴 TEST MODE: Scheduling single test notification");
            scheduleExpiryNotification(product, 5);
        } else {
            int reminderDays = getPreferredReminderDays();
            Log.d(TAG, "📅 User preferred reminder days: " + reminderDays);
            Log.d(TAG, "⏰ User preferred notification time: " +
                    String.format("%02d:%02d", getNotificationHour(), getNotificationMinute()));

            if (reminderDays > 0) {
                scheduleExpiryNotification(product, reminderDays);
            } else {
                Log.d(TAG, "ℹ️ User selected same-day notification only");
            }

            // Always schedule the day-of-expiry notification
            scheduleExpiryNotification(product, 0);
        }

        Log.d(TAG, "===== FINISHED SCHEDULING ALL NOTIFICATIONS =====\n");
    }

    /**
     * Schedule alarms for all products
     */
    public void scheduleAllAlarms(List<Product> products) {
        Log.d(TAG, "===== SCHEDULING ALARMS FOR ALL PRODUCTS =====");
        Log.d(TAG, "Total products: " + products.size());

        if (products.isEmpty()) {
            Log.d(TAG, "No products to schedule");
            return;
        }

        for (Product product : products) {
            scheduleAllNotifications(product);
        }
        Log.d(TAG, "===== FINISHED SCHEDULING ALL ALARMS =====\n");
    }

    /**
     * Cancel notification for a specific product
     */
    public void cancelNotification(Product product) {
        try {
            Log.d(TAG, "===== CANCELLING NOTIFICATION =====");
            Log.d(TAG, "Product: " + product.getName() + " (ID: " + product.getId() + ")");

            int[] reminderDays = {10, 5, 3, 1, 0};

            for (int days : reminderDays) {
                String uniqueWorkName = "expiry_" + product.getId() + "_" + days;

                // Cancel work by unique name
                workManager.cancelUniqueWork(uniqueWorkName);
                Log.d(TAG, "✅ Cancelled work: " + uniqueWorkName);
            }

            // Also cancel all work with product tag
            workManager.cancelAllWorkByTag("product_" + product.getId());
            Log.d(TAG, "✅ Cancelled all work for product ID: " + product.getId());

            Log.d(TAG, "===== CANCELLATION COMPLETE =====\n");

        } catch (Exception e) {
            Log.e(TAG, "❌ ERROR cancelling notification", e);
        }
    }

    /**
     * Cancel all notifications (useful when clearing all data)
     */
    public void cancelAllNotifications() {
        try {
            Log.d(TAG, "===== CANCELLING ALL NOTIFICATIONS =====");
            workManager.cancelAllWork();
            Log.d(TAG, "✅ Cancelled all scheduled work");
            Log.d(TAG, "===== CANCELLATION COMPLETE =====\n");
        } catch (Exception e) {
            Log.e(TAG, "❌ ERROR cancelling all notifications", e);
        }
    }
}
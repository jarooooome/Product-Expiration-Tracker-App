package com.example.productexpirationtrackerapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NotificationScheduler {

    private Context context;
    private AlarmManager alarmManager;
    private static final String TAG = "NOTIF_DEBUG";

    // TEST MODE FLAG - Set to true to test notifications in 1 minute
    private static final boolean TEST_MODE = true; // CHANGE TO false AFTER TESTING!

    public NotificationScheduler(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Log.d(TAG, "===== NOTIFICATION SCHEDULER INITIALIZED =====");
        Log.d(TAG, "Android version: " + Build.VERSION.SDK_INT);
        Log.d(TAG, "Can schedule exact alarms: " + hasExactAlarmPermission());
        Log.d(TAG, "TEST MODE: " + (TEST_MODE ? "ENABLED - 1 minute notifications" : "DISABLED"));
    }

    /**
     * Schedule notification for a product
     * @param daysBefore How many days before expiry to notify (e.g., 5 days before)
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

            Calendar notificationCalendar;

            if (TEST_MODE) {
                // TEST MODE: Schedule for 1 minute from now
                notificationCalendar = Calendar.getInstance();
                notificationCalendar.add(Calendar.MINUTE, 1);
                notificationCalendar.set(Calendar.SECOND, 0);
                notificationCalendar.set(Calendar.MILLISECOND, 0);
                Log.d(TAG, "🔴 TEST MODE ACTIVE - Scheduling for: " + notificationCalendar.getTime());
            } else {
                // NORMAL MODE: Calculate notification time based on expiry date
                notificationCalendar = Calendar.getInstance();
                notificationCalendar.setTime(expiryDate);
                notificationCalendar.add(Calendar.DAY_OF_YEAR, -daysBefore);
                notificationCalendar.set(Calendar.HOUR_OF_DAY, 9);
                notificationCalendar.set(Calendar.MINUTE, 0);
                notificationCalendar.set(Calendar.SECOND, 0);
                notificationCalendar.set(Calendar.MILLISECOND, 0);
                Log.d(TAG, "Scheduled time: " + notificationCalendar.getTime());
            }

            Log.d(TAG, "Scheduled time millis: " + notificationCalendar.getTimeInMillis());
            Log.d(TAG, "Current time: " + Calendar.getInstance().getTime());
            Log.d(TAG, "Current time millis: " + System.currentTimeMillis());

            // If the notification time is already passed, don't schedule
            if (notificationCalendar.getTimeInMillis() <= System.currentTimeMillis()) {
                Log.d(TAG, "❌ Notification time already passed for: " + product.getName());
                Log.d(TAG, "Time difference: " +
                        (notificationCalendar.getTimeInMillis() - System.currentTimeMillis()) + "ms");
                return;
            } else {
                Log.d(TAG, "✅ Notification time is in the future");
                Log.d(TAG, "Time until notification: " +
                        (notificationCalendar.getTimeInMillis() - System.currentTimeMillis()) / 1000 + " seconds");
            }

            // Check if product ID is valid
            if (product.getId() <= 0) {
                Log.e(TAG, "❌ Invalid product ID: " + product.getId());
                return;
            }

            // Create intent for the alarm receiver
            Intent intent = new Intent(context, ExpiryAlarmReceiver.class);
            intent.putExtra("product_id", product.getId());
            intent.putExtra("product_name", product.getName());
            intent.putExtra("days_left", daysBefore);
            intent.setAction("com.example.productexpirationtrackerapp.EXPIRY_ALARM_" + product.getId() + "_" + daysBefore);

            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    product.getId() * 10 + daysBefore,
                    intent,
                    flags
            );

            Log.d(TAG, "PendingIntent created: " + pendingIntent);

            // Schedule the alarm
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            notificationCalendar.getTimeInMillis(),
                            pendingIntent
                    );
                    Log.d(TAG, "✅ Alarm scheduled with setExactAndAllowWhileIdle (Android 12+)");
                } else {
                    Log.e(TAG, "❌ Cannot schedule exact alarms - permission denied");
                    alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            notificationCalendar.getTimeInMillis(),
                            pendingIntent
                    );
                    Log.d(TAG, "⚠️ Fallback: scheduled with setAndAllowWhileIdle");
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        notificationCalendar.getTimeInMillis(),
                        pendingIntent
                );
                Log.d(TAG, "✅ Alarm scheduled with setExactAndAllowWhileIdle (Android M+)");
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        notificationCalendar.getTimeInMillis(),
                        pendingIntent
                );
                Log.d(TAG, "✅ Alarm scheduled with setExact (Android KitKat+)");
            } else {
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        notificationCalendar.getTimeInMillis(),
                        pendingIntent
                );
                Log.d(TAG, "✅ Alarm scheduled with set (legacy)");
            }

            Log.d(TAG, "✅ Successfully scheduled notification for: " + product.getName() +
                    " at " + notificationCalendar.getTime());
            Log.d(TAG, "===== SCHEDULING COMPLETE =====\n");

        } catch (Exception e) {
            Log.e(TAG, "❌ ERROR scheduling notification", e);
        }
    }

    /**
     * Schedule multiple notification days (e.g., 10 days, 5 days, 1 day before)
     */
    public void scheduleAllNotifications(Product product) {
        Log.d(TAG, "===== SCHEDULING ALL NOTIFICATIONS FOR: " + product.getName() + " =====");

        if (TEST_MODE) {
            // TEST MODE: Only schedule one notification (5 days before as test)
            Log.d(TAG, "🔴 TEST MODE: Scheduling single test notification");
            scheduleExpiryNotification(product, 5);
        } else {
            // NORMAL MODE: Schedule all reminder days
            int[] reminderDays = {10, 5, 3, 1, 0};
            for (int days : reminderDays) {
                scheduleExpiryNotification(product, days);
            }
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

            Intent intent = new Intent(context, ExpiryAlarmReceiver.class);

            // Cancel all reminder days for this product
            int[] reminderDays = {10, 5, 3, 1, 0};

            for (int days : reminderDays) {
                int flags = PendingIntent.FLAG_NO_CREATE;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    flags |= PendingIntent.FLAG_IMMUTABLE;
                }

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context,
                        product.getId() * 10 + days,
                        intent,
                        flags
                );

                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                    Log.d(TAG, "✅ Cancelled notification for: " + product.getName() + " (" + days + " days)");
                } else {
                    Log.d(TAG, "No pending intent found for: " + product.getName() + " (" + days + " days)");
                }
            }

            Log.d(TAG, "===== CANCELLATION COMPLETE =====\n");

        } catch (Exception e) {
            Log.e(TAG, "❌ ERROR cancelling notification", e);
        }
    }

    /**
     * Check if the app has exact alarm permission (Android 12+)
     */
    public boolean hasExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return alarmManager.canScheduleExactAlarms();
        }
        return true;
    }
}
package com.example.productexpirationtrackerapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Handles exactly 3 notification types:
 *  1. PRODUCT_ADDED  — fires immediately when a product is added
 *  2. EXPIRY_7DAYS   — fires at 09:00 AM, 7 days before expiry date
 *  3. EXPIRY_TODAY   — fires at 08:00 AM on the expiry date itself
 */
public class NotificationScheduler {

    private final Context context;
    private final WorkManager workManager;
    private static final String TAG = "NotificationScheduler";

    // Notification channel ID — single channel for all expiry alerts
    public static final String CHANNEL_ID = "expiry_alerts";

    // Worker input data keys
    public static final String KEY_PRODUCT_ID   = "product_id";
    public static final String KEY_PRODUCT_NAME = "product_name";
    public static final String KEY_NOTIF_TYPE   = "notif_type";

    // Notification type constants (passed to worker)
    public static final String TYPE_ADDED      = "added";
    public static final String TYPE_7DAYS      = "7days";
    public static final String TYPE_EXPIRED    = "expired";

    public NotificationScheduler(Context context) {
        this.context = context;
        this.workManager = WorkManager.getInstance(context);
        ensureNotificationChannel();
    }

    // ── Channel ───────────────────────────────────────────────────────────────

    private void ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mgr =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (mgr == null) return;

            // Read vibration pattern saved by Settings
            SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            String vibPref = prefs.getString("vibration_pattern", "default");

            String channelId = "expiry_channel_" + vibPref;

            // Delete old pattern channels so cached settings don't stick
            for (String old : new String[]{
                    "expiry_alerts", "expiry_channel_default",
                    "expiry_channel_short", "expiry_channel_long", "expiry_channel_none"}) {
                mgr.deleteNotificationChannel(old);
            }

            NotificationChannel ch = new NotificationChannel(
                    channelId, "Expiry Alerts", NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("Alerts for product expiry events");

            if ("none".equals(vibPref)) {
                ch.enableVibration(false);
            } else {
                ch.enableVibration(true);
                switch (vibPref) {
                    case "short": ch.setVibrationPattern(new long[]{0, 200, 100, 200}); break;
                    case "long":  ch.setVibrationPattern(new long[]{0, 800, 200, 800}); break;
                    default:      ch.setVibrationPattern(new long[]{0, 500, 200, 500}); break;
                }
            }
            mgr.createNotificationChannel(ch);

            // Persist for workers to use
            prefs.edit().putString("notification_channel_id", channelId).apply();
        }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Call this immediately after a product is successfully added.
     * Fires an instant notification + schedules the two expiry notifications.
     */
    public void onProductAdded(Product product) {
        sendImmediateNotification(product);
        scheduleExpiryNotifications(product);
    }

    /**
     * Call this after a product is edited.
     * Cancels old scheduled notifications and reschedules from the new expiry date.
     */
    public void onProductEdited(Product product) {
        cancelScheduledNotifications(product.getId());
        scheduleExpiryNotifications(product);
    }

    /**
     * Call this when a product is deleted or consumed.
     * Cancels all pending scheduled notifications for that product.
     */
    public void onProductRemoved(int productId) {
        cancelScheduledNotifications(productId);
    }

    /**
     * Reschedule notifications for all products (called on boot / full refresh).
     */
    public void rescheduleAll(List<Product> products) {
        workManager.cancelAllWork();
        for (Product p : products) {
            scheduleExpiryNotifications(p);
        }
        Log.d(TAG, "Rescheduled notifications for " + products.size() + " products");
    }

    // ── Notification 1: Immediate (product added) ─────────────────────────────

    private void sendImmediateNotification(Product product) {
        try {
            SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            String channelId = prefs.getString("notification_channel_id", CHANNEL_ID);

            Intent intent = new Intent(context, ProductListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pi = PendingIntent.getActivity(context,
                    product.getId() * 10,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.app_logo)
                    .setContentTitle("Product Added")
                    .setContentText(product.getName() + " has been added to your tracker.")
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(product.getName() + " has been added to your tracker."))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setContentIntent(pi);

            NotificationManager mgr =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (mgr != null) {
                // Use product ID as notification ID so each product has its own
                mgr.notify(product.getId() * 10, builder.build());
                Log.d(TAG, "✅ Immediate notification sent: " + product.getName());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sending immediate notification", e);
        }
    }

    // ── Notification 2 & 3: Scheduled ────────────────────────────────────────

    private void scheduleExpiryNotifications(Product product) {
        Date expiryDate = product.getExpiryDate();
        if (expiryDate == null) {
            Log.w(TAG, "Skipping schedule — null expiry date for: " + product.getName());
            return;
        }

        // Notification 2: 7 days before expiry at 09:00 AM
        Calendar sevenDaysBefore = Calendar.getInstance();
        sevenDaysBefore.setTime(expiryDate);
        sevenDaysBefore.add(Calendar.DAY_OF_YEAR, -7);
        sevenDaysBefore.set(Calendar.HOUR_OF_DAY, 9);
        sevenDaysBefore.set(Calendar.MINUTE, 0);
        sevenDaysBefore.set(Calendar.SECOND, 0);
        sevenDaysBefore.set(Calendar.MILLISECOND, 0);
        scheduleWorker(product, TYPE_7DAYS, sevenDaysBefore, "7days");

        // Notification 3: On expiry day at 08:00 AM
        Calendar expiryDay = Calendar.getInstance();
        expiryDay.setTime(expiryDate);
        expiryDay.set(Calendar.HOUR_OF_DAY, 8);
        expiryDay.set(Calendar.MINUTE, 0);
        expiryDay.set(Calendar.SECOND, 0);
        expiryDay.set(Calendar.MILLISECOND, 0);
        scheduleWorker(product, TYPE_EXPIRED, expiryDay, "expired");
    }

    private void scheduleWorker(Product product, String type,
                                Calendar scheduledTime, String suffix) {
        long delayMs = scheduledTime.getTimeInMillis() - System.currentTimeMillis();
        if (delayMs <= 0) {
            Log.d(TAG, "Skipping past notification [" + type + "] for: " + product.getName());
            return;
        }

        String workName = "expiry_" + product.getId() + "_" + suffix;

        Data inputData = new Data.Builder()
                .putInt(KEY_PRODUCT_ID, product.getId())
                .putString(KEY_PRODUCT_NAME, product.getName())
                .putString(KEY_NOTIF_TYPE, type)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("product_" + product.getId())
                .build();

        workManager.enqueueUniqueWork(workName, ExistingWorkPolicy.REPLACE, request);
        Log.d(TAG, "✅ Scheduled [" + type + "] for " + product.getName()
                + " at " + scheduledTime.getTime());
    }

    // ── Cancel ────────────────────────────────────────────────────────────────

    private void cancelScheduledNotifications(int productId) {
        workManager.cancelAllWorkByTag("product_" + productId);

        // Also cancel the specific named workers
        workManager.cancelUniqueWork("expiry_" + productId + "_7days");
        workManager.cancelUniqueWork("expiry_" + productId + "_expired");

        // Dismiss any showing notification for this product
        NotificationManager mgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mgr != null) {
            mgr.cancel(productId * 10);      // added notification
            mgr.cancel(productId * 10 + 1);  // 7-days notification
            mgr.cancel(productId * 10 + 2);  // expired notification
        }
        Log.d(TAG, "Cancelled all notifications for product ID: " + productId);
    }
}
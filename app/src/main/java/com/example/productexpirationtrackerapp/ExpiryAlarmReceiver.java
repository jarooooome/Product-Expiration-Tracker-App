package com.example.productexpirationtrackerapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class ExpiryAlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "expiry_channel";
    private static final String CHANNEL_NAME = "Expiry Notifications";
    private static final String TAG = "NOTIF_DEBUG";

    @Override
    public void onReceive(Context context, Intent intent) {
        String productName = intent.getStringExtra("product_name");
        int productId = intent.getIntExtra("product_id", -1);
        int daysLeft = intent.getIntExtra("days_left", 0);

        Log.d(TAG, "========== ALARM RECEIVED! ==========");
        Log.d(TAG, "Product: " + productName + " (ID: " + productId + ")");
        Log.d(TAG, "Days left: " + daysLeft);

        // Recreate notification channel with latest settings
        recreateNotificationChannel(context);
        showNotification(context, productName, productId, daysLeft);
    }

    private void recreateNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);

            // DELETE existing channel para ma-apply ang bagong settings
            manager.deleteNotificationChannel(CHANNEL_ID);
            Log.d(TAG, "🗑️ Deleted existing notification channel");

            // Get user's vibration preference
            SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            String vibrationPattern = prefs.getString("vibration_pattern", "default");

            // CREATE channel with new settings
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Get notified when products are about to expire");

            // ✅ FIXED: Set vibration pattern correctly
            if (vibrationPattern.equals("none")) {
                channel.enableVibration(false);
                // ❌ REMOVED: channel.setVibrationPattern(new long[]{0});
                Log.d(TAG, "📳 Vibration: OFF");
            } else {
                channel.enableVibration(true);
                switch (vibrationPattern) {
                    case "short":
                        channel.setVibrationPattern(new long[]{0, 200, 100, 200});
                        Log.d(TAG, "📳 Vibration: SHORT");
                        break;
                    case "long":
                        channel.setVibrationPattern(new long[]{0, 800, 200, 800});
                        Log.d(TAG, "📳 Vibration: LONG");
                        break;
                    case "default":
                    default:
                        channel.setVibrationPattern(new long[]{0, 500, 200, 500});
                        Log.d(TAG, "📳 Vibration: DEFAULT");
                        break;
                }
            }

            manager.createNotificationChannel(channel);
            Log.d(TAG, "✅ Recreated notification channel with new settings");
        }
    }

    private void showNotification(Context context, String productName, int productId, int daysLeft) {
        Intent intent = new Intent(context, ProductListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("product_id", productId);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context, productId, intent, flags);

        String title;
        String message;

        if (daysLeft == 0) {
            title = "⚠️ Product Expired!";
            message = productName + " has expired today.";
        } else if (daysLeft == 1) {
            title = "⚠️ Expires Tomorrow!";
            message = productName + " will expire tomorrow.";
        } else {
            title = "📅 Product Expiring Soon";
            message = productName + " will expire in " + daysLeft + " days.";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // For Android 7.1 and below (no notification channels)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            String vibrationPattern = prefs.getString("vibration_pattern", "default");

            if (vibrationPattern.equals("none")) {
                builder.setVibrate(null);
                Log.d(TAG, "📳 Legacy - Vibration: OFF");
            } else if (vibrationPattern.equals("short")) {
                builder.setVibrate(new long[]{0, 200, 100, 200});
                Log.d(TAG, "📳 Legacy - Vibration: SHORT");
            } else if (vibrationPattern.equals("long")) {
                builder.setVibrate(new long[]{0, 800, 200, 800});
                Log.d(TAG, "📳 Legacy - Vibration: LONG");
            } else {
                builder.setVibrate(new long[]{0, 500, 200, 500});
                Log.d(TAG, "📳 Legacy - Vibration: DEFAULT");
            }
        }

        Notification notification = builder.build();

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(productId, notification);

        Log.d(TAG, "🔔 Notification shown for: " + productName);
        Log.d(TAG, "=====================================\n");
    }
}
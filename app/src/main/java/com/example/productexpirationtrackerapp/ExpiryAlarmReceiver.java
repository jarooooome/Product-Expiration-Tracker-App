package com.example.productexpirationtrackerapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class ExpiryAlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "expiry_channel";
    private static final String CHANNEL_NAME = "Expiry Notifications";
    private static final int NOTIFICATION_ID = 1000;

    @Override
    public void onReceive(Context context, Intent intent) {
        String productName = intent.getStringExtra("product_name");
        int productId = intent.getIntExtra("product_id", -1);
        int daysLeft = intent.getIntExtra("days_left", 0);

        createNotificationChannel(context);
        showNotification(context, productName, productId, daysLeft);
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Get notified when products are about to expire");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
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

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(productId, notification);
    }
}
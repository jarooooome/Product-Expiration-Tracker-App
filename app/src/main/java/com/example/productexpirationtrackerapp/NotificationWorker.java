package com.example.productexpirationtrackerapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class NotificationWorker extends Worker {

    private static final String TAG = "NotificationWorker";

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            int productId    = getInputData().getInt(NotificationScheduler.KEY_PRODUCT_ID, -1);
            String productName = getInputData().getString(NotificationScheduler.KEY_PRODUCT_NAME);
            String type      = getInputData().getString(NotificationScheduler.KEY_NOTIF_TYPE);

            if (productId == -1 || productName == null || type == null) {
                Log.e(TAG, "Invalid worker input data");
                return Result.failure();
            }

            Log.d(TAG, "Worker fired — type=" + type + ", product=" + productName);

            // Build title + body based on notification type
            String title;
            String body;

            switch (type) {
                case NotificationScheduler.TYPE_7DAYS:
                    title = "Expiring Soon";
                    body  = productName + " expires in 7 days. Use it before it's too late!";
                    break;
                case NotificationScheduler.TYPE_EXPIRED:
                    title = "Product Expired";
                    body  = productName + " has expired today. Consider removing it.";
                    break;
                default:
                    Log.w(TAG, "Unknown notification type: " + type);
                    return Result.failure();
            }

            // Get channel ID saved by NotificationScheduler
            SharedPreferences prefs = getApplicationContext()
                    .getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            String channelId = prefs.getString("notification_channel_id",
                    NotificationScheduler.CHANNEL_ID);

            // Tap notification → open ProductListActivity
            Intent intent = new Intent(getApplicationContext(), ProductListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pi = PendingIntent.getActivity(
                    getApplicationContext(),
                    productId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // Unique notification ID per product per type
            int notifId = type.equals(NotificationScheduler.TYPE_7DAYS)
                    ? productId * 10 + 1
                    : productId * 10 + 2;

            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(getApplicationContext(), channelId)
                            .setSmallIcon(R.drawable.app_logo)
                            .setContentTitle(title)
                            .setContentText(body)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setAutoCancel(true)
                            .setContentIntent(pi);

            NotificationManager mgr = (NotificationManager)
                    getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (mgr != null) {
                mgr.notify(notifId, builder.build());
                Log.d(TAG, "✅ Notification shown — " + title + " for " + productName);
            }

            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Worker failed", e);
            return Result.failure();
        }
    }
}
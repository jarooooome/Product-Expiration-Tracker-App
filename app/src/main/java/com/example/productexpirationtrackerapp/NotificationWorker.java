package com.example.productexpirationtrackerapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.util.Calendar;

public class NotificationWorker extends Worker {

    private static final String TAG = "NOTIF_WORKER";

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // Get product details from input data
            int productId = getInputData().getInt("product_id", -1);
            String productName = getInputData().getString("product_name");
            int daysLeft = getInputData().getInt("days_left", 0);

            Log.d(TAG, "===== WORKER TRIGGERED =====");
            Log.d(TAG, "Product: " + productName + " (ID: " + productId + ")");
            Log.d(TAG, "Days left: " + daysLeft);
            Log.d(TAG, "Current time: " + Calendar.getInstance().getTime());

            // Check if we have valid data
            if (productId == -1 || productName == null) {
                Log.e(TAG, "❌ Invalid product data received");
                return Result.failure();
            }

            // Create intent for the alarm receiver
            Intent intent = new Intent(getApplicationContext(), ExpiryAlarmReceiver.class);
            intent.putExtra("product_id", productId);
            intent.putExtra("product_name", productName);
            intent.putExtra("days_left", daysLeft);
            intent.setAction("com.example.productexpirationtrackerapp.EXPIRY_ALARM_" + productId + "_" + daysLeft);

            // Send broadcast
            getApplicationContext().sendBroadcast(intent);

            Log.d(TAG, "✅✅✅ WORKER EXECUTED SUCCESSFULLY");
            Log.d(TAG, "   • Notification sent for: " + productName);
            Log.d(TAG, "   • Days left: " + daysLeft);
            Log.d(TAG, "===== WORKER COMPLETE =====\n");

            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "❌❌❌ WORKER FAILED", e);
            e.printStackTrace();
            return Result.failure();
        }
    }
}
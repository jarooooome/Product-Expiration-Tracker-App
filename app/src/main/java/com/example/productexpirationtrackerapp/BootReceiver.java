package com.example.productexpirationtrackerapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("BootReceiver", "Device rebooted - rescheduling alarms");

            // Get all products from database and reschedule alarms
            AppDatabase database = AppDatabase.getDatabase(context);
            ProductDao productDao = database.productDao();

            // Run in background thread
            new Thread(() -> {
                NotificationScheduler scheduler = new NotificationScheduler(context);
                scheduler.scheduleAllAlarms(productDao.getAllProducts());
            }).start();
        }
    }
}
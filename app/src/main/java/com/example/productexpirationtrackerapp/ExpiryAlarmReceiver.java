package com.example.productexpirationtrackerapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ExpiryAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "ExpiryAlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Notifications are now built and shown inside NotificationWorker.
        // This receiver intentionally does nothing — all scheduling goes
        // through NotificationScheduler → WorkManager → NotificationWorker.
        Log.d(TAG, "ExpiryAlarmReceiver triggered (no-op — handled by NotificationWorker)");
    }
}
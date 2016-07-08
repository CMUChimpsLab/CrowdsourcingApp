package com.dhchoi.crowdsourcingapp.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.dhchoi.crowdsourcingapp.NotificationHelper;
import com.dhchoi.crowdsourcingapp.activities.MainActivity;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received Alarm");

        NotificationHelper.createNotification(
                "Keep Calm",
                "And Use CitySourcing App!",
                context,
                MainActivity.class);
    }
}

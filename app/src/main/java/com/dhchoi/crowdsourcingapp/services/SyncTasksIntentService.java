package com.dhchoi.crowdsourcingapp.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.ResultReceiver;

public class SyncTasksIntentService extends IntentService {

    private static final String TAG = "SyncTasksIntentService";

    protected ResultReceiver mReceiver; // The receiver where results are forwarded from this service.

    public SyncTasksIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    /**
     * Tries to sync tasks with the server. If successful, sends an array of tasks to a result receiver.
     * If unsuccessful, sends an error message instead.
     * Note: We define a {@link android.os.ResultReceiver} in * MainActivity to process content sent from this service.
     * <p/>
     * This service calls this method from the default worker thread with the intent that started
     * the service. When this method returns, the service automatically stops.
     */
    @Override
    protected void onHandleIntent(Intent intent) {

    }
}

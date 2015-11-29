package com.dhchoi.crowdsourcingapp.services;

import android.os.Bundle;
import android.util.Log;

import com.dhchoi.crowdsourcingapp.NotificationHelper;
import com.dhchoi.crowdsourcingapp.activities.MainActivity;
import com.google.android.gms.gcm.GcmListenerService;

public class GcmMessageListenerService extends GcmListenerService {

    private static final String TAG = "GcmMsgListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Bundle Data: " + data);

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */
//        simpleGeofenceManager.setGeofence(id,
//                name,
//                lat,
//                lng,
//                radius,
//                GEOFENCE_EXPIRATION_TIME,
//                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        NotificationHelper.createNotification("New task available", data.getString("name", "Touch to check new task."), this, MainActivity.class);
        // [END_EXCLUDE]
    }
    // [END receive_message]
}

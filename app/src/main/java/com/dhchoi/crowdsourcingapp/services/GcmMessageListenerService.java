package com.dhchoi.crowdsourcingapp.services;

import static com.dhchoi.crowdsourcingapp.Constants.GEOFENCE_EXPIRATION_TIME;
import static com.dhchoi.crowdsourcingapp.Constants.NOTIFICATION_TITLE;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.dhchoi.crowdsourcingapp.R;
import com.dhchoi.crowdsourcingapp.SimpleGeofenceManager;
import com.dhchoi.crowdsourcingapp.activities.MainActivity;
import com.google.android.gms.gcm.GcmListenerService;
import com.google.android.gms.location.Geofence;

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
        Log.d(TAG, "Bundle Data: " + data);

        SimpleGeofenceManager geofenceManager = new SimpleGeofenceManager(this);
        String name = data.getString("name", "DEFAULT");
        double lat = Double.valueOf(data.getString("lat", "40.4472512"));
        double lng = Double.valueOf(data.getString("lng", "-79.9460148"));
        float radius = Float.valueOf(data.getString("radius", "60.0f"));

        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Name: " + name);
        Log.d(TAG, "Lat: " + lat);
        Log.d(TAG, "Lng: " + lng);
        Log.d(TAG, "Radius: " + radius);

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
        geofenceManager.setGeofence(name,
                lat,
                lng,
                radius,
                GEOFENCE_EXPIRATION_TIME,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        sendNotification("Received New Geofence: " + name);
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(NOTIFICATION_TITLE)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}

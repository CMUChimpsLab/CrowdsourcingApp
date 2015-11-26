package com.dhchoi.crowdsourcingapp.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.dhchoi.crowdsourcingapp.R;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import static com.dhchoi.crowdsourcingapp.Constants.NOTIFICATION_TITLE;
import static com.dhchoi.crowdsourcingapp.Constants.TAG;

public class GeofenceTransitionsIntentService extends IntentService {

    public GeofenceTransitionsIntentService() {
        super(GeofenceTransitionsIntentService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Handles incoming intents.
     *
     * @param intent The Intent sent by Location Services. This Intent is provided to Location
     *               Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geoFenceEvent = GeofencingEvent.fromIntent(intent);

        if (geoFenceEvent.hasError()) {
            int errorCode = geoFenceEvent.getErrorCode();
            Log.e(TAG, "Location Services error: " + errorCode);
        } else {
            final int transitionType = geoFenceEvent.getGeofenceTransition();
            // Get the geofence id triggered. Note that only one geofence can be triggered at a
            // time in this example, but in some cases you might want to consider the full list of geofences triggered.
            final String triggeredGeoFenceId = geoFenceEvent.getTriggeringGeofences().get(0).getRequestId();

            Intent fetchAddressIntent = new Intent(this, FetchAddressIntentService.class);
            fetchAddressIntent.putExtra(FetchAddressIntentService.FETCH_ADDRESS_RESULT_RECEIVER, new ResultReceiver(new Handler(Looper.getMainLooper())) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    String locationString = resultData.getString(FetchAddressIntentService.FETCH_ADDRESS_RESULT_DATA_KEY);
                    if (Geofence.GEOFENCE_TRANSITION_ENTER == transitionType) {
                        showToast(R.string.entering_geofence);
                        createNotification(triggeredGeoFenceId, "Entered: " + triggeredGeoFenceId + " (" + locationString + ")");
                    } else if (Geofence.GEOFENCE_TRANSITION_EXIT == transitionType) {
                        showToast(R.string.exiting_geofence);
                        createNotification(triggeredGeoFenceId, "Exited: " + triggeredGeoFenceId + " (" + locationString + ")");
                    }
                }
            });
            fetchAddressIntent.putExtra(FetchAddressIntentService.FETCH_ADDRESS_LOCATION_DATA_EXTRA, geoFenceEvent.getTriggeringLocation());
            startService(fetchAddressIntent);
        }
    }

    /**
     * Showing a toast message, using the Main thread
     */
    private void showToast(final int resourceId) {
        final Context context = getApplicationContext();
        final Handler mainThread = new Handler(Looper.getMainLooper());
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, context.getString(resourceId), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Creates a notification
     */
    private void createNotification(String tag, String notificationText) {
        int notificationId = 1;

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(NOTIFICATION_TITLE)
                .setContentText(notificationText)
                .setAutoCancel(true);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(tag, notificationId, mBuilder.build());
    }
}

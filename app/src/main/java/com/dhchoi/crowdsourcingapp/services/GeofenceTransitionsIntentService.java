package com.dhchoi.crowdsourcingapp.services;

import static com.dhchoi.crowdsourcingapp.Constants.TAG;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.dhchoi.crowdsourcingapp.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceTransitionsIntentService extends IntentService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public GeofenceTransitionsIntentService() {
        super(GeofenceTransitionsIntentService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();

//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addApi(LocationServices.API)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .build();
    }

    /**
     * Handles incoming intents.
     * @param intent The Intent sent by Location Services. This Intent is provided to Location
     * Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geoFenceEvent = GeofencingEvent.fromIntent(intent);

        if (geoFenceEvent.hasError()) {
            int errorCode = geoFenceEvent.getErrorCode();
            Log.e(TAG, "Location Services error: " + errorCode);
        }
        else {
            int transitionType = geoFenceEvent.getGeofenceTransition();

            if (Geofence.GEOFENCE_TRANSITION_ENTER == transitionType) {

                // Connect to the Google Api service in preparation for sending a DataItem.
//                mGoogleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
//
//                // Get the geofence id triggered. Note that only one geofence can be triggered at a
//                // time in this example, but in some cases you might want to consider the full list of geofences triggered.
//                String triggeredGeoFenceId = geoFenceEvent.getTriggeringGeofences().get(0).getRequestId();
//
//                // Create a DataItem with this geofence's id. The wearable can use this to create a notification.
//                final PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(GEOFENCE_DATA_ITEM_PATH);
//                putDataMapRequest.getDataMap().putString(KEY_GEOFENCE_ID, triggeredGeoFenceId);
//                if (mGoogleApiClient.isConnected()) {
//                    Wearable.DataApi.putDataItem(
//                            mGoogleApiClient, putDataMapRequest.asPutDataRequest()).await();
//                }
//                else {
//                    Log.e(TAG, "Failed to send data item: " + putDataMapRequest + " - Client disconnected from Google Play Services");
//                }
//                Toast.makeText(this, getString(R.string.entering_geofence), Toast.LENGTH_SHORT).show();
//                mGoogleApiClient.disconnect();
                showToast(this, R.string.entering_geofence);
                Log.i(TAG, "entered geofence: " + geoFenceEvent.getTriggeringLocation().toString());
                createNotification("Entered Geofence: " + geoFenceEvent.toString());
            }
            else if (Geofence.GEOFENCE_TRANSITION_EXIT == transitionType) {
//                // Delete the data item when leaving a geofence region.
//                mGoogleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
//                Wearable.DataApi.deleteDataItems(mGoogleApiClient, GEOFENCE_DATA_ITEM_URI).await();
//                showToast(this, R.string.exiting_geofence);
//                mGoogleApiClient.disconnect();
                showToast(this, R.string.exiting_geofence);
            }
        }
    }

    /**
     * Showing a toast message, using the Main thread
     */
    private void showToast(final Context context, final int resourceId) {
        Handler mainThread = new Handler(Looper.getMainLooper());
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, context.getString(resourceId), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNotification(String notificationText) {
        String notificationTitle = "Geofence Alert";
        int notificationId = 1;

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(notificationId, mBuilder.build());
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int cause) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}

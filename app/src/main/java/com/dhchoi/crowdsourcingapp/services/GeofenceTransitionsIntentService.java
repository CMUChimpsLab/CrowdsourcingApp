package com.dhchoi.crowdsourcingapp.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.dhchoi.crowdsourcingapp.Constants;
import com.dhchoi.crowdsourcingapp.NotificationHelper;
import com.dhchoi.crowdsourcingapp.activities.TaskManagementActivity;
import com.dhchoi.crowdsourcingapp.task.Task;
import com.dhchoi.crowdsourcingapp.task.TaskManager;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

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
        Log.d(Constants.TAG, "Start handling geofence transition event");
        GeofencingEvent geoFenceEvent = GeofencingEvent.fromIntent(intent);

        if (geoFenceEvent.hasError()) {
            int errorCode = geoFenceEvent.getErrorCode();
            Log.e(Constants.TAG, "Location Services error: " + errorCode);
        } else {
            int transitionType = geoFenceEvent.getGeofenceTransition();
            for (Geofence triggeredGeofence : geoFenceEvent.getTriggeringGeofences()) {
                // taskId and geofence.requestId are identical
                Task triggeredTask = TaskManager.getTaskById(this, triggeredGeofence.getRequestId());
                String taskId = triggeredTask.getId();
                String taskName = triggeredTask.getName();
                String taskLocationName = triggeredTask.getLocation().getName();

                Log.d(Constants.TAG, "Build notification for task: " + taskName);
                if (Geofence.GEOFENCE_TRANSITION_ENTER == transitionType) {
                    NotificationHelper.createNotification("Enabled: " + taskName, taskLocationName, taskId, this, TaskManagementActivity.class);
                } else if (Geofence.GEOFENCE_TRANSITION_EXIT == transitionType) {
                    NotificationHelper.createNotification("Disabled: " + taskName, taskLocationName, taskId, this, TaskManagementActivity.class);
                } else {
                    Log.d(Constants.TAG, "Discarding Geofence transition type: " + transitionType);
                }
            }
        }
    }
}

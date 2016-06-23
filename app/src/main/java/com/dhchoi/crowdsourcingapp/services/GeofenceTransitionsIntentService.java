package com.dhchoi.crowdsourcingapp.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.dhchoi.crowdsourcingapp.Constants;
import com.dhchoi.crowdsourcingapp.NotificationHelper;
import com.dhchoi.crowdsourcingapp.activities.MainActivity;
import com.dhchoi.crowdsourcingapp.task.Task;
import com.dhchoi.crowdsourcingapp.task.TaskManager;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeofenceTransitionsIntentService extends IntentService {

    private static final String TAG = "GeofenceService";

    public static final String GEOFENCE_TRANSITION_BROADCAST = Constants.PACKAGE_NAME + ".GEOFENCE_TRANSITION_BROADCAST";
    public static final String ACTIVATED_TASK_ID_KEY = Constants.PACKAGE_NAME + ".ACTIVATED_TASK_ID_KEY";
    public static final String INACTIVATED_TASK_ID_KEY = Constants.PACKAGE_NAME + ".INACTIVATED_TASK_ID_KEY";

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
            Log.e(Constants.TAG, "Location Services error: " + GeofenceStatusCodes.getStatusCodeString(geoFenceEvent.getErrorCode()));
            return;
        }

        List<String> activatedTaskIds = new ArrayList<>();
        List<String> inactivatedTaskIds = new ArrayList<>();
        int transitionType = geoFenceEvent.getGeofenceTransition();

        for (Geofence triggeredGeofence : geoFenceEvent.getTriggeringGeofences()) {
            // taskId and geofence.requestId are identical
            Task triggeredTask = TaskManager.getTaskById(this, triggeredGeofence.getRequestId());
            String taskId = triggeredTask.getId();
            String taskName = triggeredTask.getName();
            String taskLocationName = triggeredTask.getLocation().getName();

            Log.d(Constants.TAG, "Build notification for task: " + taskName);
            if (Geofence.GEOFENCE_TRANSITION_ENTER == transitionType) {
                Log.d(TAG, "Geofence Enter");

                if (!triggeredTask.isActivated()) {
                    NotificationHelper.createNotification("Activated: " + taskName, taskLocationName, taskId, this, MainActivity.class);
                    triggeredTask.setActivated(true);
                    activatedTaskIds.add(taskId);
                    inactivatedTaskIds.remove(taskId);
                }
            } else if (Geofence.GEOFENCE_TRANSITION_EXIT == transitionType) {
                Log.d(TAG, "Geofence Exit");

                if (triggeredTask.isActivated()) {
                    NotificationHelper.createNotification("Inactivated: " + taskName, taskLocationName, taskId, this, MainActivity.class);
                    triggeredTask.setActivated(false);
                    inactivatedTaskIds.add(taskId);
                    activatedTaskIds.remove(taskId);
                }
            } else {
                Log.d(Constants.TAG, "Discarding Geofence transition type: " + transitionType);
            }

            TaskManager.updateTask(this, triggeredTask);
        }

        // Notify any listener that geofence transition has occurred
        String[] activatedTaskIdsToStringArr = activatedTaskIds.toArray(new String[activatedTaskIds.size()]);
        String[] inactivatedTaskIdsToStringArr = inactivatedTaskIds.toArray(new String[inactivatedTaskIds.size()]);

        Log.d(Constants.TAG, "Broadcast triggered activatedTaskIds" + Arrays.toString(activatedTaskIdsToStringArr));
        Log.d(Constants.TAG, "Broadcast triggered inactivatedTaskIds" + Arrays.toString(inactivatedTaskIdsToStringArr));

        Intent geofenceTransitionIntent = new Intent(GEOFENCE_TRANSITION_BROADCAST);
        geofenceTransitionIntent.putExtra(ACTIVATED_TASK_ID_KEY, activatedTaskIdsToStringArr);
        geofenceTransitionIntent.putExtra(INACTIVATED_TASK_ID_KEY, inactivatedTaskIdsToStringArr);
        LocalBroadcastManager.getInstance(this).sendBroadcast(geofenceTransitionIntent);
    }
}

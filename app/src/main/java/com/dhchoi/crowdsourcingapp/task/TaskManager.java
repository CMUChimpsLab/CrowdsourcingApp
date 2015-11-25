package com.dhchoi.crowdsourcingapp.task;

import com.dhchoi.crowdsourcingapp.activities.BaseGoogleApiActivity;
import com.dhchoi.crowdsourcingapp.simplegeofence.SimpleGeofence;
import com.dhchoi.crowdsourcingapp.simplegeofence.SimpleGeofenceManager;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import static com.dhchoi.crowdsourcingapp.Constants.GEOFENCE_EXPIRATION_TIME;
import static com.dhchoi.crowdsourcingapp.Constants.PACKAGE_NAME;

public class TaskManager {

    public static final String TASK_KEY_ID = PACKAGE_NAME + ".TASK_KEY_UID";

    private List<Task> mTasksList;

    public TaskManager() {
        // HARD CODED FOR EXAMPLE
        Task homeTask = new Task("task-id-1", "Checking Refrigerator", 0, new SimpleGeofence(
                "geofence-id-1",
                "Home",
                40.4472512,
                -79.9460148,
                60.0f,
                GEOFENCE_EXPIRATION_TIME,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT
        ));
        homeTask.addTaskAction(new TaskAction("Is there any milk left?", TaskAction.ResponseType.TEXT));

        mTasksList = new ArrayList<Task>();
        mTasksList.add(homeTask);
    }

    public Task getTaskById(String id) {
        for (Task t : mTasksList) {
            if (t.getId().equals(id)) {
                return t;
            }
        }

        return null;
    }

    public List<Task> getTasksList() {
        return mTasksList;
    }

    public void activateAllTasks(BaseGoogleApiActivity baseGoogleApiActivity) {
        List<Geofence> geofenceList = new ArrayList<Geofence>();
        for (Task t : mTasksList) {
            geofenceList.add(t.getLocation().toGeofence());
        }

        LocationServices.GeofencingApi.addGeofences(
                baseGoogleApiActivity.getGoogleApiClient(),
                geofenceList,
                SimpleGeofenceManager.getGeofenceTransitionPendingIntent(baseGoogleApiActivity));
    }

}

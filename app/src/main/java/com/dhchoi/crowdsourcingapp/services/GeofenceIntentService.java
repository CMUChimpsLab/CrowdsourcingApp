package com.dhchoi.crowdsourcingapp.services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.dhchoi.crowdsourcingapp.Constants;
import com.dhchoi.crowdsourcingapp.task.Task;
import com.dhchoi.crowdsourcingapp.task.TaskManager;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GeofenceIntentService extends IntentService {

    private static final String TAG = "GeofenceIntentService";

    public static final String LOCATION_AGENT_BROADCAST = Constants.PACKAGE_NAME + ".LOCATION_AGENT_BROADCAST";
    public static final String ACTIVATED_TASK_ID_KEY = Constants.PACKAGE_NAME + ".ACTIVATED_TASK_ID_KEY";
    public static final String INACTIVATED_TASK_ID_KEY = Constants.PACKAGE_NAME + ".INACTIVATED_TASK_ID_KEY";

    private static List<Task> mGeofenceList = new ArrayList<>();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * Used to name the worker thread, important only for debugging.
     */
    public GeofenceIntentService() {
        super(TAG);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Service started");

        LatLng newLocation = new Gson().fromJson(intent.getDataString(), LatLng.class);

        List<String> activatedTaskIds = new ArrayList<>();
        List<String> inactivatedTaskIds = new ArrayList<>();

        Log.d(TAG, "Geofences: " + mGeofenceList.toString());
        
        for (int i = 0; i < mGeofenceList.size(); i++) {
            Task task = mGeofenceList.get(i);
            LatLng existingLocation = task.getLocation().getLatLng();

            Log.d(TAG, "Task ID: " + task.getId() + " Distance: " + getDistanceFromLatLng(newLocation, existingLocation) + " Radius: " + task.getRadius());

            if ((int) getDistanceFromLatLng(newLocation, existingLocation) <= task.getRadius()) {
                task.setActivated(true);
                activatedTaskIds.add(task.getId());
                TaskManager.updateTask(this, task);
            } else {
                task.setActivated(false);
                inactivatedTaskIds.add(task.getId());
                TaskManager.updateTask(this, task);
            }
        }

        Intent locationAgentIntent = new Intent(LOCATION_AGENT_BROADCAST);
        locationAgentIntent.putStringArrayListExtra(ACTIVATED_TASK_ID_KEY, (ArrayList<String>) activatedTaskIds);
        locationAgentIntent.putStringArrayListExtra(INACTIVATED_TASK_ID_KEY, (ArrayList<String>) inactivatedTaskIds);
        LocalBroadcastManager.getInstance(this).sendBroadcast(locationAgentIntent);
    }

    /**
     * Add single task to Geofence list
     * @param task to be added
     */
    public static void addGeofence(Task task) {
        if (task != null) {
            for (Task t : mGeofenceList) {
                if (t.getId().equals(task.getId()))
                    return;
            }
            mGeofenceList.add(task);
        }
    }

    /**
     * Add collection of tasks to Geofence list
     * @param tasks to be added
     */
    public static void addGeofences(Collection<Task> tasks) {
        if (tasks != null && !tasks.isEmpty()) {
            for (Task task : tasks)
                addGeofence(task);
        }
    }

    /**
     * Remove single task to Geofence list
     * @param task to be removed
     */
    public static void removeGeofence(Task task) {
        if (task != null) {
            String removeId = task.getId();
            Log.d(TAG, "Removing Geofence " + removeId + "...");

            for (int i = 0; i < mGeofenceList.size(); i++) {
                if (mGeofenceList.get(i).getId().equals(removeId))
                    mGeofenceList.remove(i);
            }

            Log.d(TAG, "Geofences: " + mGeofenceList);
        }
    }

    /**
     * Remove collection of tasks to Geofence list
     * @param tasks to be removed
     */
    public static void removeGeofences(Collection<Task> tasks) {
        if (tasks != null && !tasks.isEmpty()) {
            for (Task task : tasks)
                removeGeofence(task);
        }
    }


    /***
     * Calculate distance in meters given two LatLngs
     * @param location1 first location
     * @param location2 second location
     * @return          distance between 2 locations
     */
    public static double getDistanceFromLatLng(LatLng location1, LatLng location2) {
        float[] results = new float[1];
        Location.distanceBetween(location1.latitude, location1.longitude, location2.latitude, location2.longitude, results);
        return results[0];
    }

    public static List<Task> getGeofenceList() {
        return mGeofenceList;
    }

    /***
     * Handle location change
     */
    public static class LocationChangeListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            Log.i(Constants.TAG, "Provider: " + location.getProvider());
            Log.i(Constants.TAG, "Latitude: " + Double.toString(location.getLatitude()));
            Log.i(Constants.TAG, "Longitude: " + Double.toString(location.getLongitude()));
        }

    }

}

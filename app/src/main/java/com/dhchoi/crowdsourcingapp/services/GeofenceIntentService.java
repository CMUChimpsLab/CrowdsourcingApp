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

        for (Task task : mGeofenceList) {
            LatLng existingLocation = task.getLocation().getLatLng();

            Log.d(TAG, "Task ID: " + task.getId() + " Distance: " + getDistanceFromLatLng(newLocation, existingLocation) + " Radius: " + task.getRadius());

            if ((int)getDistanceFromLatLng(newLocation, existingLocation) <= task.getRadius()) {
                activatedTaskIds.add(task.getId());
                task.setActivated(true);
                TaskManager.updateTask(this, task);
            } else {
                inactivatedTaskIds.add(task.getId());
                task.setActivated(false);
                TaskManager.updateTask(this, task);
            }
        }

        Intent locationAgentIntent = new Intent(LOCATION_AGENT_BROADCAST);
        locationAgentIntent.putStringArrayListExtra(ACTIVATED_TASK_ID_KEY, (ArrayList<String>) activatedTaskIds);
        locationAgentIntent.putStringArrayListExtra(INACTIVATED_TASK_ID_KEY, (ArrayList<String>) inactivatedTaskIds);
        LocalBroadcastManager.getInstance(this).sendBroadcast(locationAgentIntent);
    }

    public static void addGeofence(Task task) {
        if (task != null) {
            for (Task t : mGeofenceList) {
                if (t.getId().equals(task.getId()))
                    return;
            }
            mGeofenceList.add(task);
        }
    }

    public static void addGeofences(Collection<Task> tasks) {
        if (tasks != null && !tasks.isEmpty()) {
            for (Task task : tasks)
                addGeofence(task);
        }
    }

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
    private static double getDistanceFromLatLng(LatLng location1, LatLng location2) {
        double lat1 = location1.latitude;
        double lng1 = location1.longitude;
        double lat2 = location2.latitude;
        double lng2 = location2.longitude;

        double earthRadius = 6371000;           // earth radius in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng1 - lng2);
        double sinDLat = Math.sin(dLat / 2);
        double sinDLng = Math.sin(dLng / 2);

        double a = Math.pow(sinDLat, 2) + Math.pow(sinDLng, 2)
                 * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
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

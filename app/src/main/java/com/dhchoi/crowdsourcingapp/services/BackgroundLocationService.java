package com.dhchoi.crowdsourcingapp.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.dhchoi.crowdsourcingapp.NotificationHelper;
import com.dhchoi.crowdsourcingapp.activities.MainActivity;
import com.dhchoi.crowdsourcingapp.task.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class BackgroundLocationService extends Service {

    private static final String TAG = "LocationService";
    private static final String LOCATION_CHANGE = "location_change";

    private LocationManager locationManager;
    private static final String GPS = LocationManager.GPS_PROVIDER;
    private static final String NETWORK = LocationManager.NETWORK_PROVIDER;

    private int minInterval = 1000 * 5;         // 5 seconds
    private float minDistance = 5.0f;           // 10 meters?

    private List<Task> mGeofenceList;

    private BackgroundLocationListener[] locationListeners = new BackgroundLocationListener[] {
            new BackgroundLocationListener(LocationManager.NETWORK_PROVIDER),
            new BackgroundLocationListener(LocationManager.GPS_PROVIDER)
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressWarnings("all")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Started");

        String dataStr = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getString("geofencelist", new Gson().toJson(new ArrayList<Task>()));
        mGeofenceList = new Gson().fromJson(dataStr, new TypeToken<List<Task>>() {}.getType());
        Log.d(TAG, mGeofenceList.toString());

        try {
            locationManager.requestLocationUpdates(
                    locationManager.isProviderEnabled(GPS) ? GPS : NETWORK,
                    minInterval,
                    minDistance,
                    locationListeners[locationManager.isProviderEnabled(GPS) ? 1 : 0]);
                    // use the best available
        } catch (Exception e) {
            e.printStackTrace();
        }

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "Created");

        initialiseLocationManager();

        super.onCreate();
    }

    private void initialiseLocationManager() {
        Log.i(TAG, "Location Manager Initialised");
        if (locationManager == null)
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
    }

    @SuppressWarnings("all")
    @Override
    public void onDestroy() {
        Log.i(TAG, "Destroyed");
        if (locationManager != null) {
            for (int i = 0; i < locationListeners.length; i++) {
                try {
                    locationManager.removeUpdates(locationListeners[i]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        super.onDestroy();
    }

    private class BackgroundLocationListener implements LocationListener {

        private static final String TAG = "LocationListener";
        private static final int TWO_MINUTES = 1000 * 60 * 2;

        Location lastKnownLocation;

        public BackgroundLocationListener(String provider) {
            lastKnownLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, location.getLatitude() + " " + location.getLongitude());

            if (isBetterLocation(location, lastKnownLocation)) {
                lastKnownLocation = new Location(location);

                boolean notify = false;

                for (int i = 0; i < mGeofenceList.size(); i++) {
                    Task task = mGeofenceList.get(i);
                    Location taskLocation = new Location("Task");
                    taskLocation.setLatitude(task.getLocation().getLatitude());
                    taskLocation.setLongitude(task.getLocation().getLongitude());
                    if (mGeofenceList.get(i).getRadius() >= lastKnownLocation.distanceTo(taskLocation)) {
                        // inside
                        if (!task.isActivated()) {
                            // became activated
                            task.setActivated(true);
                            notify = true;
                        }
                    } else {
                        task.setActivated(false);
                    }
                }

                if (notify) {
                    NotificationHelper.createNotification(
                            "A Task Just Became Available",
                            "You just entered the active area of another task",
                            getApplicationContext(),
                            MainActivity.class);
                }
            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.i(TAG, "Status Changed");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.i(TAG, "Provider Enabled");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i(TAG, "Provider Disabled");
        }

        /***
         * Check whether a new location fix is better than the old one
         * @param newLocation           new location
         * @param lastKnownLocation     current best location
         * @return                      whether new locaiton is better
         */
        private boolean isBetterLocation(Location newLocation, Location lastKnownLocation) {
            if (lastKnownLocation == null)
                return true;

            // whether the new location fix is newer or older
            long timeDelta = newLocation.getTime() - lastKnownLocation.getTime();
            boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
            boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
            boolean isNewer = timeDelta > 0;

            if (isSignificantlyNewer)
                return true;
            else if (isSignificantlyOlder)
                return false;

            // whether the new location fix is more or less accurate
            int accuracyDelta = (int) (newLocation.getAccuracy() - lastKnownLocation.getAccuracy());
            boolean isLessAccurate = accuracyDelta > 0;
            boolean isMoreAccurate = accuracyDelta < 0;
            boolean isSignificantlyLessAccurate = accuracyDelta > 200;

            boolean isFromSameProvider = isSameProvider(newLocation.getProvider(), lastKnownLocation.getProvider());

            if (isMoreAccurate)
                return true;
            else if (isNewer && !isLessAccurate)
                return true;
            else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider)
                return true;

            return false;
        }

        private boolean isSameProvider(String provider1, String provider2) {
            if (provider1 == null)
                return provider2 == null;
            return provider1.equals(provider2);
        }
    }
}

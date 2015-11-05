package com.dhchoi.crowdsourcingapp;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.dhchoi.crowdsourcingapp.services.GeofenceTransitionsIntentService;
import com.google.android.gms.location.Geofence;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.dhchoi.crowdsourcingapp.Constants.KEY_ID_SET;
import static com.dhchoi.crowdsourcingapp.Constants.SHARED_PREFERENCES;

public class SimpleGeofenceManager {
    private Context applicationContext;
    // Persistent storage for geofences.
    private SimpleGeofenceStore mGeofenceStorage;
    private final SharedPreferences mPrefs;

    public SimpleGeofenceManager(Context context) {
        applicationContext = context;
        mGeofenceStorage = new SimpleGeofenceStore(applicationContext);
        mPrefs = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    /**
     * Create internal "flattened" objects containing the geofence data.
     */
    public void setGeofence(String geofenceId, double latitude, double longitude, float radius,
                            long expiration, int transition) {
        mGeofenceStorage.setGeofence(geofenceId, new SimpleGeofence(
                geofenceId,
                latitude,
                longitude,
                radius,
                expiration,
                transition
        ));
        saveGeofenceId(geofenceId);
    }

    public SimpleGeofence getGeofence(String id) {
        return mGeofenceStorage.getGeofence(id);
    }

    /**
     * Create a PendingIntent that triggers GeofenceTransitionIntentService
     * when a geofence transition occurs.
     */
    public PendingIntent getGeofenceTransitionPendingIntent() {
        Intent intent = new Intent(applicationContext, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public List<Geofence> getGeofenceList() {
        List<Geofence> geofenceList = new ArrayList();
        for(String geofenceId : getSavedGeofenceIdSet()) {
            geofenceList.add(getGeofence(geofenceId).toGeofence());
        }
        return geofenceList;
    }

    private void saveGeofenceId(String id) {
        SharedPreferences.Editor prefs = mPrefs.edit();
        Set<String> geofenceIdSet = getSavedGeofenceIdSet();
        geofenceIdSet.add(id);
        prefs.putStringSet(KEY_ID_SET, geofenceIdSet);
        prefs.commit();
    }

    private Set<String> getSavedGeofenceIdSet() {
        return mPrefs.getStringSet(KEY_ID_SET, new HashSet<String>());
    }
}

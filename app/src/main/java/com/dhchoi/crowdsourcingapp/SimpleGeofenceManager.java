package com.dhchoi.crowdsourcingapp;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.dhchoi.crowdsourcingapp.services.GeofenceTransitionsIntentService;
import com.google.android.gms.location.Geofence;

import java.util.ArrayList;
import java.util.List;

import static com.dhchoi.crowdsourcingapp.Constants.GEOFENCE_EXPIRATION_TIME;

public class SimpleGeofenceManager {
    Context applicationContext;
    // Persistent storage for geofences.
    SimpleGeofenceStore mGeofenceStorage;
    // Internal List of Geofence objects. In a real app, these might be provided by an API based on locations within the user's proximity.
    List<Geofence> mGeofenceList;

    public SimpleGeofenceManager(Context context) {
        applicationContext = context;
        mGeofenceStorage = new SimpleGeofenceStore(applicationContext);
        mGeofenceList = new ArrayList<Geofence>();
    }

    /**
     * In this sample, the geofences are predetermined and are hard-coded here.
     * A real app might dynamically create geofences based on the user's location.
     */
    public void createGeofences() {
        // These will store hard-coded geofences in this sample app.

        // Create internal "flattened" objects containing the geofence data.
        String mHomeGeofenceId = "home";
        SimpleGeofence mHomeGeofence = new SimpleGeofence(
                mHomeGeofenceId,
                40.447222,
                -79.946714,
                60.0f,
                GEOFENCE_EXPIRATION_TIME,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT
        );
        mGeofenceStorage.setGeofence(mHomeGeofenceId, mHomeGeofence);
        mGeofenceList.add(mHomeGeofence.toGeofence());

        String mGatesCenterGeofenceId = "gatesCenter";
        SimpleGeofence mGatesCenterGeofence = new SimpleGeofence(
                mGatesCenterGeofenceId,
                40.443336,
                -79.944675,
                60.0f,
                GEOFENCE_EXPIRATION_TIME,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT
        );
        mGeofenceStorage.setGeofence(mGatesCenterGeofenceId, mGatesCenterGeofence);
        mGeofenceList.add(mGatesCenterGeofence.toGeofence());
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
        return mGeofenceList;
    }
}

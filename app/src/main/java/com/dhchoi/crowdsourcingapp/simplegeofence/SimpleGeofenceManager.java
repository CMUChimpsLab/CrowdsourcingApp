package com.dhchoi.crowdsourcingapp.simplegeofence;

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

import static com.dhchoi.crowdsourcingapp.Constants.PACKAGE_NAME;
import static com.dhchoi.crowdsourcingapp.Constants.SHARED_PREFERENCES;

public class SimpleGeofenceManager {

    // Keys for flattened geofences stored in SharedPreferences.
    public static final String KEY_ID = PACKAGE_NAME + ".KEY_UID";
    public static final String KEY_NAME = PACKAGE_NAME + ".KEY_NAME";
    public static final String KEY_QUESTION = PACKAGE_NAME + ".KEY_QUESTION";
    public static final String KEY_LATITUDE = PACKAGE_NAME + ".KEY_LATITUDE";
    public static final String KEY_LONGITUDE = PACKAGE_NAME + ".KEY_LONGITUDE";
    public static final String KEY_RADIUS = PACKAGE_NAME + ".KEY_RADIUS";
    public static final String KEY_EXPIRATION_DURATION = PACKAGE_NAME + ".KEY_EXPIRATION_DURATION";
    public static final String KEY_TRANSITION_TYPE = PACKAGE_NAME + ".KEY_TRANSITION_TYPE";
    // The prefix for flattened geofence keys.
    public static final String KEY_PREFIX = PACKAGE_NAME + ".KEY";
    // Key for geofence id set
    public static final String KEY_ID_SET = PACKAGE_NAME + ".ID_SET";

    // Invalid values, used to test geofence storage when retrieving geofences.
    public static final long INVALID_LONG_VALUE = -999l;
    public static final float INVALID_FLOAT_VALUE = -999.0f;
    public static final int INVALID_INT_VALUE = -999;
    public static final String INVALID_STRING_VALUE = "none";

    // The SharedPreferences object in which geofences are stored.
    private final SharedPreferences mPrefs;

    /**
     * Create the SharedPreferences storage with private access only.
     */
    public SimpleGeofenceManager(Context context) {
        mPrefs = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    /**
     * Returns a stored geofence by its id, or returns null if it's not found.
     *
     * @param id The ID of a stored geofence.
     * @return A SimpleGeofence defined by its center and radius, or null if the ID is invalid.
     */
    public SimpleGeofence getGeofence(String id) {
        // Get the latitude for the geofence identified by id, or INVALID_FLOAT_VALUE if it doesn't
        // exist (similarly for the other values that follow).
        String name = mPrefs.getString(getGeofenceFieldKey(id, KEY_NAME), INVALID_STRING_VALUE);
        double lat = mPrefs.getFloat(getGeofenceFieldKey(id, KEY_LATITUDE), INVALID_FLOAT_VALUE);
        double lng = mPrefs.getFloat(getGeofenceFieldKey(id, KEY_LONGITUDE), INVALID_FLOAT_VALUE);
        float radius = mPrefs.getFloat(getGeofenceFieldKey(id, KEY_RADIUS), INVALID_FLOAT_VALUE);
        long expirationDuration = mPrefs.getLong(getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION), INVALID_LONG_VALUE);
        int transitionType = mPrefs.getInt(getGeofenceFieldKey(id, KEY_TRANSITION_TYPE), INVALID_INT_VALUE);
        // If none of the values is incorrect, return the object.
        if (!name.equals(INVALID_STRING_VALUE)
                && lat != INVALID_FLOAT_VALUE
                && lng != INVALID_FLOAT_VALUE
                && radius != INVALID_FLOAT_VALUE
                && expirationDuration != INVALID_LONG_VALUE
                && transitionType != INVALID_INT_VALUE) {
            return new SimpleGeofence(id, name, lat, lng, radius, expirationDuration, transitionType);
        }

        // Otherwise, return null.
        return null;
    }

    public void setGeofence(String id, String name, double latitude, double longitude, float radius,
                            long expiration, int transition) {
        setGeofence(id, new SimpleGeofence(
                id,
                name,
                latitude,
                longitude,
                radius,
                expiration,
                transition
        ));
    }

    /**
     * Save a geofence.
     *
     * @param geofence The SimpleGeofence with the values you want to save in SharedPreferences.
     */
    private void setGeofence(String id, SimpleGeofence geofence) {
        // Get a SharedPreferences editor instance. Among other things, SharedPreferences
        // ensures that updates are atomic and non-concurrent.
        SharedPreferences.Editor prefs = mPrefs.edit();
        // Write the Geofence values to SharedPreferences.
        prefs.putString(getGeofenceFieldKey(id, KEY_NAME), geofence.getName());
        prefs.putFloat(getGeofenceFieldKey(id, KEY_LATITUDE), (float) geofence.getLatitude());
        prefs.putFloat(getGeofenceFieldKey(id, KEY_LONGITUDE), (float) geofence.getLongitude());
        prefs.putFloat(getGeofenceFieldKey(id, KEY_RADIUS), geofence.getRadius());
        prefs.putLong(getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION), geofence.getExpirationDuration());
        prefs.putInt(getGeofenceFieldKey(id, KEY_TRANSITION_TYPE), geofence.getTransitionType());
        // Add the geofenceId to the set of saved geofence Ids
        Set<String> geofenceIdSet = getSavedGeofenceIdSet();
        geofenceIdSet.add(id);
        prefs.putStringSet(KEY_ID_SET, geofenceIdSet);
        // Commit the changes.
        prefs.apply();
    }

    /**
     * Remove a flattened geofence object from storage by removing all of its keys.
     */
    public void clearGeofence(String id) {
        SharedPreferences.Editor prefs = mPrefs.edit();
        // Remove the Geofence values from SharedPreferences.
        prefs.remove(getGeofenceFieldKey(id, KEY_NAME));
        prefs.remove(getGeofenceFieldKey(id, KEY_LATITUDE));
        prefs.remove(getGeofenceFieldKey(id, KEY_LONGITUDE));
        prefs.remove(getGeofenceFieldKey(id, KEY_RADIUS));
        prefs.remove(getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION));
        prefs.remove(getGeofenceFieldKey(id, KEY_TRANSITION_TYPE));
        // Remove the geofenceId from the set of saved geofence Ids
        Set<String> geofenceIdSet = getSavedGeofenceIdSet();
        geofenceIdSet.remove(id);
        prefs.putStringSet(KEY_ID_SET, geofenceIdSet);
        // Commit the changes.
        prefs.apply();
    }

    public List<Geofence> getGeofenceList() {
        List<Geofence> geofenceList = new ArrayList();
        for (String geofenceId : getSavedGeofenceIdSet()) {
            geofenceList.add(getGeofence(geofenceId).toGeofence());
        }
        return geofenceList;
    }

    /**
     * Create a PendingIntent that triggers GeofenceTransitionIntentService
     * when a geofence transition occurs.
     */
    public static PendingIntent getGeofenceTransitionPendingIntent(Context context) {
        Intent intent = new Intent(context, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Given a Geofence object's ID and the name of a field (for example, KEY_LATITUDE), return
     * the key name of the object's values in SharedPreferences.
     *
     * @param id        The ID of a Geofence object.
     * @param fieldName The field represented by the key.
     * @return The full key name of a value in SharedPreferences.
     */
    private String getGeofenceFieldKey(String id, String fieldName) {
        return KEY_PREFIX + "_" + id + "_" + fieldName;
    }

    private Set<String> getSavedGeofenceIdSet() {
        return mPrefs.getStringSet(KEY_ID_SET, new HashSet<String>());
    }
}

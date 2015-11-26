package com.dhchoi.crowdsourcingapp.simplegeofence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.dhchoi.crowdsourcingapp.services.GeofenceTransitionsIntentService;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SimpleGeofence implements Serializable {

    @SerializedName("name")
    private final String mName;
    @SerializedName("lat")
    private final double mLatitude;
    @SerializedName("lng")
    private final double mLongitude;
    @SerializedName("radius")
    private final float mRadius;

    /**
     * @param name         The Geofence's request name.
     * @param latitude   Latitude of the Geofence's center in degrees.
     * @param longitude  Longitude of the Geofence's center in degrees.
     * @param radius     Radius of the geofence circle in meters.
     */
    public SimpleGeofence(String name, double latitude, double longitude, float radius) {
        mName = name;
        mLatitude = latitude;
        mLongitude = longitude;
        mRadius = radius;
    }

    public String getName() {
        return mName;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public float getRadius() {
        return mRadius;
    }

    public String getId() {
        return toString();
    }

    /**
     * Creates a Location Services Geofence object from a SimpleGeofence.
     *
     * @return A Geofence object.
     */
    public Geofence toGeofence() {
        // Build a new Geofence object.
        return new Geofence.Builder()
                .setRequestId(getId())
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .setCircularRegion(mLatitude, mLongitude, mRadius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }

    @Override
    public String toString() {
        return mName + "-" + mLatitude + "-" + mLongitude + "-" + mRadius;
    }

    public static GeofencingRequest getGeofencingRequest(Geofence geofence) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(geofence);
        return builder.build();
    }

    /**
     * Create a PendingIntent that triggers GeofenceTransitionIntentService when a geofence transition occurs.
     */
    public static PendingIntent getGeofenceTransitionPendingIntent(Context context) {
        Intent intent = new Intent(context, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}

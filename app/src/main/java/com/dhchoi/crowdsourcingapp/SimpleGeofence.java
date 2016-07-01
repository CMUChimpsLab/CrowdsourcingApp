package com.dhchoi.crowdsourcingapp;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SimpleGeofence implements Serializable {

    @SerializedName("name")
    private final String mName;
    @SerializedName("taskId")
    private String mTaskId;
    @SerializedName("lat")
    private final double mLatitude;
    @SerializedName("lng")
    private final double mLongitude;
    @SerializedName("radius")
    private final float mRadius;

    /**
     * @param name      The Geofence's request name.
     * @param taskId    The ID of the task that owns this Geofence.
     * @param latitude  Latitude of the Geofence's center in degrees.
     * @param longitude Longitude of the Geofence's center in degrees.
     * @param radius    Radius of the geofence circle in meters.
     */
    public SimpleGeofence(String name, String taskId, double latitude, double longitude, float radius) {
        mName = name;
        mTaskId = taskId;
        mLatitude = latitude;
        mLongitude = longitude;
        mRadius = radius;
    }

    public String getName() {
        return mName;
    }

    public String getTaskId() {
        return mTaskId;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public LatLng getLatLng() {
        return new LatLng(getLatitude(), getLongitude());
    }

    public float getRadius() {
        return mRadius;
    }

    /**
     * Creates a Location Services Geofence object from a SimpleGeofence.
     *
     * @return A Geofence object.
     */
    public Geofence toGeofence() {
        // Build a new Geofence object.
        return new Geofence.Builder()
                .setRequestId(getTaskId())
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .setCircularRegion(mLatitude, mLongitude, mRadius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }

    @Override
    public String toString() {
        return mTaskId + "-" + mName + "-" + mLatitude + "-" + mLongitude + "-" + mRadius;
    }

}

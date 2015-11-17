package com.dhchoi.crowdsourcingapp;

import com.google.android.gms.location.Geofence;

public class Constants {

    private Constants() {}

    public static final String TAG = "CrowdsourcingApp";
    public static final String PACKAGE_NAME = "com.dhchoi.crowdsourcingapp";
    public static final String NOTIFICATION_TITLE = "CrowdsourcingApp";
    // The name of the SharedPreferences.
    public static final String SHARED_PREFERENCES = "SharedPreferences";

    public final static int PERMISSION_REQUEST = 2;
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 8000;
    // Request code to attempt to resolve Google Play services connection failures.
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    // Timeout for making a connection to GoogleApiClient (in milliseconds).
    public static final long CONNECTION_TIME_OUT_MS = 100;

    // For the purposes of this demo, the geofences are hard-coded and should not expire.
    // An app with dynamically-created geofences would want to include a reasonable expiration time.
    public static final long GEOFENCE_EXPIRATION_TIME = Geofence.NEVER_EXPIRE;

    // Keys for flattened geofences stored in SharedPreferences.
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

    // For FetchAddressIntentService
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";

    // For PlacePicker
    public static final int PLACE_PICKER_REQUEST = 1;
}

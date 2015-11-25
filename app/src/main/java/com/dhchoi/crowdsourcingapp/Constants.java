package com.dhchoi.crowdsourcingapp;

import com.google.android.gms.location.Geofence;

public class Constants {

    private Constants() {
    }

    public static final String TAG = "CrowdsourcingApp";
    public static final String PACKAGE_NAME = "com.dhchoi.crowdsourcingapp";
    public static final String NOTIFICATION_TITLE = "CrowdsourcingApp";
    public static final String SHARED_PREFERENCES = "SharedPreferences";

    public static final int PERMISSION_REQUEST = 2;
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 8000;
    public static final String SENT_GCM_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String GCM_REGISTRATION_COMPLETE = "registrationComplete";
    // Request code to attempt to resolve Google Play services connection failures.
    public static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    // Timeout for making a connection to GoogleApiClient (in milliseconds).
    public static final long CONNECTION_TIME_OUT_MS = 100;

    // For the purposes of this demo, the geofences are hard-coded and should not expire.
    // An app with dynamically-created geofences would want to include a reasonable expiration time.
    public static final long GEOFENCE_EXPIRATION_TIME = Geofence.NEVER_EXPIRE;

    // For FetchAddressIntentService
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";

    // For PlacePicker
    public static final int PLACE_PICKER_REQUEST = 1;
}

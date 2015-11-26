package com.dhchoi.crowdsourcingapp;

public class Constants {

    private Constants() {
    }

    public static final String TAG = "CrowdsourcingApp";
    public static final String PACKAGE_NAME = "com.dhchoi.crowdsourcingapp";
    public static final String NOTIFICATION_TITLE = "CrowdsourcingApp";

    public static final String APP_SERVER_BASE_URL = "http://10.0.0.16:3000";
    public static final String APP_SERVER_TEST_URL = APP_SERVER_BASE_URL + "/test";
    public static final String APP_SERVER_SYNC_URL = APP_SERVER_BASE_URL + "/sync";
    public static final String APP_SERVER_FETCH_TASKS_URL = APP_SERVER_BASE_URL + "/fetch-tasks";

    public static final int APP_PERMISSIONS_REQUEST = 2;
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 8000;
    public static final String SENT_GCM_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String GCM_REGISTRATION_COMPLETE = "registrationComplete";
    // Request code to attempt to resolve Google Play services connection failures.
    public static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    // Timeout for making a connection to GoogleApiClient (in milliseconds).
    public static final long CONNECTION_TIME_OUT_MS = 100;

}

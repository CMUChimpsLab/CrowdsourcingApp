package com.dhchoi.crowdsourcingapp;

public class Constants {

    private Constants() {
    }

    public static final String TAG = "CrowdsourcingApp";
    public static final String PACKAGE_NAME = "com.dhchoi.crowdsourcingapp";
    public static final String DEFAULT_SHARED_PREF = PACKAGE_NAME + ".DEFAULT_SHARED_PREF";
    public static final String USER_LOGGED_IN = PACKAGE_NAME + ".USER_LOGGED_IN";
    public static final String USER_ID_KEY = PACKAGE_NAME + ".USER_ID_KEY";
    public static final String USER_GCM_TOKEN_KEY = PACKAGE_NAME + ".USER_GCM_TOKEN_KEY";

    public static final String APP_SERVER_BASE_URL = "http://ec2-54-221-193-1.compute-1.amazonaws.com:3000";
    public static final String APP_SERVER_TASK_CREATE_URL = APP_SERVER_BASE_URL + "/db/task-add";
    public static final String APP_SERVER_TASK_COMPLETE_URL = APP_SERVER_BASE_URL + "/db/task-respond";
    public static final String APP_SERVER_TASK_SYNC_URL = APP_SERVER_BASE_URL + "/db/task-sync";
    public static final String APP_SERVER_TASK_FETCH_URL = APP_SERVER_BASE_URL + "/db/task-fetch";
    public static final String APP_SERVER_USER_CREATE_URL = APP_SERVER_BASE_URL + "/db/user-create";
    public static final String APP_SERVER_USER_FETCH_URL = APP_SERVER_BASE_URL + "/db/user-fetch";

}

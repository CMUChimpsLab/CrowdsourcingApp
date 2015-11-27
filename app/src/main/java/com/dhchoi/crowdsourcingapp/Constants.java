package com.dhchoi.crowdsourcingapp;

public class Constants {

    private Constants() {
    }

    public static final String TAG = "CrowdsourcingApp";
    public static final String PACKAGE_NAME = "com.dhchoi.crowdsourcingapp";
    public static final String NOTIFICATION_TITLE = "CrowdsourcingApp";

    public static final String APP_SERVER_BASE_URL = "http://ec2-54-221-193-1.compute-1.amazonaws.com:3000";
    public static final String APP_SERVER_TASK_RESPOND_URL = APP_SERVER_BASE_URL + "/db/task-respond";
    public static final String APP_SERVER_TASK_SYNC_URL = APP_SERVER_BASE_URL + "/db/task-sync";
    public static final String APP_SERVER_TASK_FETCH_URL = APP_SERVER_BASE_URL + "/db/task-fetch";

}

package com.dhchoi.crowdsourcingapp.user;

import android.content.Context;
import android.content.SharedPreferences;

import static com.dhchoi.crowdsourcingapp.Constants.PACKAGE_NAME;

public class UserManager {

    private static final String USER_SHARED_PREF = PACKAGE_NAME + ".USER_SHARED_PREF";
    private static final String USER_LOGGED_IN = PACKAGE_NAME + ".USER_LOGGED_IN";
    private static final String USER_ID_KEY = PACKAGE_NAME + ".USER_ID_KEY";
    private static final String USER_GCM_TOKEN_KEY = PACKAGE_NAME + ".USER_GCM_TOKEN_KEY";

    private UserManager() {
    }

    public static void setUserId(Context context, String userId) {
        getSharedPreferences(context).edit().putString(USER_ID_KEY, userId).apply();
    }

    public static void setUserGcmToken(Context context, String gcmToken) {
        getSharedPreferences(context).edit().putString(USER_GCM_TOKEN_KEY, gcmToken).apply();
    }

    public static void setUserLoggedIn(Context context, boolean isLoggedIn) {
        getSharedPreferences(context).edit().putBoolean(USER_LOGGED_IN, isLoggedIn).apply();
    }

    public static String getUserId(Context context) {
        return getSharedPreferences(context).getString(USER_ID_KEY, "");
    }

    public static String getGcmToken(Context context) {
        return getSharedPreferences(context).getString(USER_GCM_TOKEN_KEY, "");
    }

    public static boolean isUserLoggedIn(Context context) {
        return getSharedPreferences(context).getBoolean(USER_LOGGED_IN, false);
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(USER_SHARED_PREF, context.MODE_PRIVATE);
    }
}

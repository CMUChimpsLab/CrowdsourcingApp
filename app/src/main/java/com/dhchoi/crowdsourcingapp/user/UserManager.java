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

    /**
     * Sets the user's id.
     *
     * @param context of the app
     * @param userId  to be set
     */
    public static void setUserId(Context context, String userId) {
        getSharedPreferences(context).edit().putString(USER_ID_KEY, userId).apply();
    }

    /**
     * Sets the user's gcm token.
     *
     * @param context  of the app
     * @param gcmToken to be set
     */
    public static void setUserGcmToken(Context context, String gcmToken) {
        getSharedPreferences(context).edit().putString(USER_GCM_TOKEN_KEY, gcmToken).apply();
    }

    /**
     * Sets whether the user has logged in.
     *
     * @param context    of the app
     * @param isLoggedIn the login status of the user
     */
    public static void setUserLoggedIn(Context context, boolean isLoggedIn) {
        getSharedPreferences(context).edit().putBoolean(USER_LOGGED_IN, isLoggedIn).apply();
    }

    /**
     * Returns the user's id.
     *
     * @param context of the app
     * @return the user's id
     */
    public static String getUserId(Context context) {
        return getSharedPreferences(context).getString(USER_ID_KEY, "");
    }

    /**
     * Returns the user's gcm token.
     *
     * @param context of the app
     * @return the user's gcm token
     */
    public static String getUserGcmToken(Context context) {
        return getSharedPreferences(context).getString(USER_GCM_TOKEN_KEY, "");
    }

    /**
     * Returns a boolean indicating whether the user has logged in.
     *
     * @param context of the app
     * @return true if the user is logged in
     */
    public static boolean isUserLoggedIn(Context context) {
        return getSharedPreferences(context).getBoolean(USER_LOGGED_IN, false);
    }

    /**
     * Resets all user data to default empty values.
     *
     * @param context of the app
     */
    public static void reset(Context context) {
        setUserId(context, "");
        setUserGcmToken(context, "");
        setUserLoggedIn(context, false);
    }

    /**
     * Returns the shared preferences that manages user data.
     *
     * @param context of the app
     * @return the shared preferences that manages user data
     */
    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(USER_SHARED_PREF, context.MODE_PRIVATE);
    }
}

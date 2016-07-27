package com.dhchoi.crowdsourcingapp.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.dhchoi.crowdsourcingapp.Constants;
import com.dhchoi.crowdsourcingapp.HttpClientCallable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.dhchoi.crowdsourcingapp.Constants.PACKAGE_NAME;

public class UserManager {

    private static final String TAG = "UserManager";

    private static final String USER_SHARED_PREF = PACKAGE_NAME + ".USER_SHARED_PREF";
    private static final String USER_LOGGED_IN = PACKAGE_NAME + ".USER_LOGGED_IN";
    private static final String USER_ID_KEY = PACKAGE_NAME + ".USER_ID_KEY";
    private static final String USER_GCM_TOKEN_KEY = PACKAGE_NAME + ".USER_GCM_TOKEN_KEY";
    private static final String USER_BALANCE = PACKAGE_NAME  + ".USER_BALANCE";

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
     * Sets the balance of the user
     *
     * @param context   of the app
     * @param balance   current balance of the user
     */
    public static void setUserBalance(Context context, float balance) {
        getSharedPreferences(context).edit().putFloat(USER_BALANCE, balance).apply();
    }

    /**
     * Returns the user's id.
     *
     * @param context of the app
     * @return the user's id
     */
    public static String getUserId(Context context) {
        return getSharedPreferences(context).getString(USER_ID_KEY, null);
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
     * Returns the current balance of the user (default 20)
     * @param context of the app
     * @return current balance of the user
     */
    public static float getUserBalance(Context context) {
        return getSharedPreferences(context).getFloat(USER_BALANCE, 20);
    }

    /**
     * Syncs the current user with the server
     * @param context of the app
     * @return current user
     */
    public static boolean syncUser(Context context) {
        try {
            Map<String, String> userParams = new HashMap<>();
            userParams.put("userId", UserManager.getUserId(context));
            String syncResponse = HttpClientCallable.Executor.execute(new HttpClientCallable(Constants.APP_SERVER_USER_FETCH_URL, HttpClientCallable.GET, userParams));
            if (syncResponse != null) {
                JSONObject syncResponseObj = new JSONObject(syncResponse);

                double userBalance = (float) syncResponseObj.getDouble("balance");
                String userId = syncResponseObj.getString("id");

                setUserBalance(context, (float) userBalance);

                Log.d(TAG, "User Sync Success");
                Log.d(TAG, "User ID: " + userId);
                Log.d(TAG, "Current Balance: " + userBalance);

                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
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
        return context.getSharedPreferences(USER_SHARED_PREF, Context.MODE_PRIVATE);
    }
}

package com.dhchoi.crowdsourcingapp.task;

import android.content.Context;
import android.content.SharedPreferences;

import com.dhchoi.crowdsourcingapp.simplegeofence.SimpleGeofence;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.dhchoi.crowdsourcingapp.Constants.PACKAGE_NAME;

public class TaskManager {

    public static final String TASK_SHARED_PREF = PACKAGE_NAME + ".TASK_SHARED_PREF";
    public static final String TASK_KEY_ID = PACKAGE_NAME + ".TASK_KEY_ID";
    public static final String TASK_KEY_ID_SET = PACKAGE_NAME + ".TASK_KEY_ID_SET";

    public static Task getTaskById(Context context, String id) {
        return new Gson().fromJson(getSharedPreferences(context).getString(getTaskKeyById(id), ""), Task.class);
    }

    private static Task getTaskById(SharedPreferences sharedPreferences, String id) {
        return new Gson().fromJson(sharedPreferences.getString(getTaskKeyById(id), ""), Task.class);
    }

    public static void removeTaskById(Context context, GoogleApiClient googleApiClient, String id) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();

        // cancel geofence
        Task task = getTaskById(sharedPreferences, id);
        List<String> geofenceId = new ArrayList<String>();
        geofenceId.add(task.getLocation().getId());
        LocationServices.GeofencingApi.removeGeofences(googleApiClient, geofenceId);

        // remove id
        prefsEditor.remove(getTaskKeyById(task.getId()));

        // remove id from saved id set
        Set<String> savedTaskIdsSet = getSavedTaskIdsSet(sharedPreferences);
        savedTaskIdsSet.remove(task.getId());
        prefsEditor.putStringSet(TASK_KEY_ID_SET, savedTaskIdsSet);
        prefsEditor.apply();
    }

    public static List<Task> getAllTasks(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);

        List<Task> tasks = new ArrayList<Task>();
        for (String id : getSavedTaskIdsSet(sharedPreferences)) {
            tasks.add(getTaskById(sharedPreferences, id));
        }
        return tasks;
    }

    public static List<Task> setTasks(String jsonArray, Context context, GoogleApiClient googleApiClient) {
        List<Task> tasks = new Gson().fromJson(jsonArray, new TypeToken<ArrayList<Task>>() {}.getType());

        SharedPreferences mPrefs = getSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Set<String> savedTaskIdsSet = getSavedTaskIdsSet(mPrefs);

        for (Task t : tasks) {
            prefsEditor.putString(getTaskKeyById(t.getId()), new Gson().toJson(t));
            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    SimpleGeofence.getGeofencingRequest(t.getLocation().toGeofence()),
                    SimpleGeofence.getGeofenceTransitionPendingIntent(context));
            savedTaskIdsSet.add(t.getId());
        }

        prefsEditor.putStringSet(TASK_KEY_ID_SET, savedTaskIdsSet);
        prefsEditor.apply();

        return tasks;
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(TASK_SHARED_PREF, context.MODE_PRIVATE);
    }

    private static String getTaskKeyById(String id) {
        return TASK_KEY_ID + "_" + id;
    }

    private static Set<String> getSavedTaskIdsSet(SharedPreferences sharedPreferences) {
        return sharedPreferences.getStringSet(TASK_KEY_ID_SET, new HashSet<String>());
    }
}

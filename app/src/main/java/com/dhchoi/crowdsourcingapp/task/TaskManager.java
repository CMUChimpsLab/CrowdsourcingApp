package com.dhchoi.crowdsourcingapp.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.dhchoi.crowdsourcingapp.Constants;
import com.dhchoi.crowdsourcingapp.HttpClientCallable;
import com.dhchoi.crowdsourcingapp.SimpleGeofence;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.dhchoi.crowdsourcingapp.Constants.PACKAGE_NAME;

public class TaskManager {
    private static final String TAG = "TaskManager";

    public static final String TASK_SHARED_PREF = PACKAGE_NAME + ".TASK_SHARED_PREF";
    public static final String TASK_KEY_ID = PACKAGE_NAME + ".TASK_KEY_ID";
    public static final String TASK_KEY_ID_SET = PACKAGE_NAME + ".TASK_KEY_ID_SET";
    public static final String TASKS_LAST_UPDATED = PACKAGE_NAME + ".TASKS_LAST_UPDATED";

    private static final String JSON_FIELD_LAST_UPDATED = "lastUpdated";
    private static final String JSON_FIELD_CHANGES = "changes";
    private static final String JSON_FIELD_STATUS = "status";
    private static final String JSON_FIELD_STATUS_CREATED = "created";
    private static final String JSON_FIELD_STATUS_DELETED = "deleted";
    private static final String JSON_FIELD_TASK_ID = "taskId";

    private static List<OnTasksUpdatedListener> mOnTasksUpdatedListeners = new ArrayList<OnTasksUpdatedListener>();

    private TaskManager() {
    }

    public static void addOnTaskUpdatedListener(OnTasksUpdatedListener listener) {
        mOnTasksUpdatedListeners.add(listener);
    }

    public static void removeOnTaskUpdatedListener(OnTasksUpdatedListener listener) {
        mOnTasksUpdatedListeners.remove(listener);
    }

    public static Task getTaskById(Context context, String id) {
        return getTaskById(getSharedPreferences(context), id);
    }

    private static Task getTaskById(SharedPreferences sharedPreferences, String id) {
        return new Gson().fromJson(sharedPreferences.getString(getTaskKeyById(id), ""), Task.class);
    }

    public static List<Task> getAllTasks(Context context) {
        return getAllTasks(getSharedPreferences(context));
    }

    private static List<Task> getAllTasks(SharedPreferences sharedPreferences) {
        List<Task> tasks = new ArrayList<Task>();
        for (String id : getSavedTaskIdsSet(sharedPreferences)) {
            tasks.add(getTaskById(sharedPreferences, id));
        }
        return tasks;
    }

    public static void setTasks(Context context, GoogleApiClient googleApiClient, String jsonArray) {
        setTasks(context, getSharedPreferences(context), googleApiClient, jsonArray);
    }

    private static void setTasks(Context context, SharedPreferences sharedPreferences, GoogleApiClient googleApiClient, String jsonArray) {
        try {
            SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
            Set<String> savedTaskIdsSet = getSavedTaskIdsSet(sharedPreferences);

            // create list of tasks from json string
            List<Task> tasks = new Gson().fromJson(jsonArray, new TypeToken<ArrayList<Task>>() {
            }.getType());

            for (Task t : tasks) {
                // save task id
                prefsEditor.putString(getTaskKeyById(t.getId()), new Gson().toJson(t));

                // save task id to saved tasks id set
                savedTaskIdsSet.add(t.getId());

                // start geofence
                LocationServices.GeofencingApi.addGeofences(
                        googleApiClient,
                        SimpleGeofence.getGeofencingRequest(t.getLocation().toGeofence()),
                        SimpleGeofence.getGeofenceTransitionPendingIntent(context));
            }

            prefsEditor.putStringSet(TASK_KEY_ID_SET, savedTaskIdsSet);
            prefsEditor.apply();

            for (OnTasksUpdatedListener listener : mOnTasksUpdatedListeners) {
                listener.onTasksAdded(tasks);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private static void removeTasks(SharedPreferences sharedPreferences, GoogleApiClient googleApiClient, List<String> taskIds) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        Set<String> savedTaskIdsSet = getSavedTaskIdsSet(sharedPreferences);

        for (String id : taskIds) {
            Task task = getTaskById(sharedPreferences, id);
            if (task != null) {
                // remove task id
                prefsEditor.remove(getTaskKeyById(task.getId()));

                // remove task id from saved tasks id set
                savedTaskIdsSet.remove(task.getId());

                // cancel geofence
                List<String> geofenceId = new ArrayList<String>();
                geofenceId.add(task.getLocation().getTaskId());
                LocationServices.GeofencingApi.removeGeofences(googleApiClient, geofenceId);
            }
        }

        prefsEditor.putStringSet(TASK_KEY_ID_SET, savedTaskIdsSet);
        prefsEditor.apply();

        for (OnTasksUpdatedListener listener : mOnTasksUpdatedListeners) {
            listener.onTasksDeleted(taskIds);
        }
    }

    public static void syncTasks(Context context, GoogleApiClient googleApiClient) {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences(context);
            final long appLastUpdatedTime = getLastUpdatedTime(sharedPreferences);

            Log.d(TAG, "check sync availability with server. appLastUpdatedTime: " + appLastUpdatedTime);

            // check sync availability
            Map<String, String> syncParams = new HashMap<String, String>();
            syncParams.put(JSON_FIELD_LAST_UPDATED, String.valueOf(appLastUpdatedTime));
            String syncResponse = HttpClientCallable.Executor.execute(new HttpClientCallable(Constants.APP_SERVER_TASK_SYNC_URL, HttpClientCallable.GET, syncParams));
            if (syncResponse != null) {
                JSONObject syncResponseObj = new JSONObject(syncResponse);
                long serverLastUpdatedTime = syncResponseObj.getLong(JSON_FIELD_LAST_UPDATED);
                JSONArray changes = syncResponseObj.getJSONArray(JSON_FIELD_CHANGES);

                Log.d(TAG, "serverLastUpdatedTime: " + serverLastUpdatedTime);
                Log.d(TAG, "changes: " + changes.toString());

                // fetch changes
                if (appLastUpdatedTime < serverLastUpdatedTime) {
                    Log.d(TAG, "start fetching changes");

                    // update appLastUpdatedTime to serverLastUpdatedTime
                    saveLastUpdatedTime(sharedPreferences, serverLastUpdatedTime);

                    List<String> tasksCreated = new ArrayList<String>();
                    List<String> tasksDeleted = new ArrayList<String>();
                    for (int i = 0; i < changes.length(); i++) {
                        String taskId = changes.getJSONObject(i).getString(JSON_FIELD_TASK_ID);
                        String taskStatus = changes.getJSONObject(i).getString(JSON_FIELD_STATUS);
                        if (taskStatus.equals(JSON_FIELD_STATUS_CREATED)) {
                            tasksCreated.add(taskId);
                        }
                        if (taskStatus.equals(JSON_FIELD_STATUS_DELETED)) {
                            tasksDeleted.add(taskId);
                        }
                    }

                    // no need to deal with tasks that were deleted after being created
                    for (String id : tasksDeleted) {
                        if (tasksCreated.contains(id)) {
                            tasksCreated.remove(id);
                            tasksDeleted.remove(id);
                        }
                    }

                    // remove deleted tasks
                    removeTasks(sharedPreferences, googleApiClient, tasksDeleted);

                    // fetch and set new tasks
                    Map<String, String> fetchParams = new HashMap<String, String>();
                    fetchParams.put(JSON_FIELD_TASK_ID, tasksCreated.toString());
                    String fetchResponse = HttpClientCallable.Executor.execute(new HttpClientCallable(Constants.APP_SERVER_TASK_FETCH_URL, HttpClientCallable.GET, fetchParams));
                    if (fetchResponse != null) {
                        setTasks(context, sharedPreferences, googleApiClient, fetchResponse);
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
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

    private static long getLastUpdatedTime(SharedPreferences sharedPreferences) {
        return sharedPreferences.getLong(TASKS_LAST_UPDATED, 0);
    }

    private static void saveLastUpdatedTime(SharedPreferences sharedPreferences, long time) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putLong(TASKS_LAST_UPDATED, time);
        prefsEditor.apply();
    }

    public interface OnTasksUpdatedListener {
        void onTasksAdded(List<Task> addedTasks);

        void onTasksDeleted(List<String> deletedTaskIds);
    }
}

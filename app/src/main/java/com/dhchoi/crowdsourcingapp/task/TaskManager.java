package com.dhchoi.crowdsourcingapp.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.dhchoi.crowdsourcingapp.Constants;
import com.dhchoi.crowdsourcingapp.HttpClientCallable;
import com.dhchoi.crowdsourcingapp.SimpleGeofence;
import com.dhchoi.crowdsourcingapp.user.UserManager;
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

/**
 * TaskManager that manages all {@link Task}s within the app.
 */
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

    /**
     * Returns the {@link Task} object for the corresponding task id.
     *
     * @param context of the app
     * @param id      of the {@link Task}
     * @return the {@link Task} object for the corresponding task id
     */
    public static Task getTaskById(Context context, String id) {
        return new Gson().fromJson(getSharedPreferences(context).getString(getTaskKeyById(id), ""), Task.class);
    }

    /**
     * Returns a list of all {@link Task}s that have been created by all users.
     *
     * @param context of the app
     * @return a list of all {@link Task}s that have been created
     */
    public static List<Task> getAllTasks(Context context) {
        List<Task> tasks = new ArrayList<Task>();
        for (String id : getSavedTaskIdsSet(context)) {
            tasks.add(getTaskById(context, id));
        }
        return tasks;
    }

    /**
     * Removes all {@link Task}s from the app and resets the last time that the app synced with the server.
     *
     * @param context         of the app
     * @param googleApiClient to be used for Google services
     */
    public static void reset(Context context, GoogleApiClient googleApiClient) {
        saveLastUpdatedTime(context, 0);
        removeTasks(context, googleApiClient, new ArrayList<String>(getSavedTaskIdsSet(context)));
    }

    /**
     * Updates a {@link Task} by rewriting it with the new data.
     *
     * @param context of the app
     * @param task    to be updated
     */
    public static void updateTask(Context context, Task task) {
        getSharedPreferences(context).edit().putString(getTaskKeyById(task.getId()), new Gson().toJson(task)).apply();
    }

    /**
     * Creates a list of {@link Task}s from its string representation and
     *
     * @param context         of the app
     * @param googleApiClient to be used for Google services
     * @param jsonArray       JSON array of {@link Task}s by its string representation
     * @throws SecurityException
     */
    private static void setTasks(Context context, GoogleApiClient googleApiClient, String jsonArray) throws SecurityException {
        try {
            SharedPreferences.Editor prefsEditor = getSharedPreferences(context).edit();
            Set<String> savedTaskIdsSet = getSavedTaskIdsSet(context);
            String userId = UserManager.getUserId(context);

            // create list of tasks from json string
            List<Task> allTasks = new Gson().fromJson(jsonArray, new TypeToken<ArrayList<Task>>() {
            }.getType());
            List<Task> addedTasks = new ArrayList<Task>();
            List<Task> ownedTasks = new ArrayList<Task>();

            for (Task t : allTasks) {
                // save task id
                prefsEditor.putString(getTaskKeyById(t.getId()), new Gson().toJson(t));

                // save task id to saved tasks id set
                savedTaskIdsSet.add(t.getId());

                if (!userId.equals(t.getOwner())) {
                    // start geofence
                    LocationServices.GeofencingApi.addGeofences(
                            googleApiClient,
                            SimpleGeofence.getGeofencingRequest(t.getLocation().toGeofence()),
                            SimpleGeofence.getGeofenceTransitionPendingIntent(context));
                    addedTasks.add(t);
                } else {
                    ownedTasks.add(t);
                }
            }

            prefsEditor.putStringSet(TASK_KEY_ID_SET, savedTaskIdsSet).apply();

            for (OnTasksUpdatedListener listener : mOnTasksUpdatedListeners) {
                listener.onTasksAdded(addedTasks);
                listener.onTasksCreated(ownedTasks);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Removes {@link Task}s from the app.
     *
     * @param context         of the app
     * @param googleApiClient to be used for Google services
     * @param taskIds         list of {@link Task} ids to be removed
     */
    private static void removeTasks(Context context, GoogleApiClient googleApiClient, List<String> taskIds) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        Set<String> savedTaskIdsSet = getSavedTaskIdsSet(context);

        for (String id : taskIds) {
            Task task = getTaskById(context, id);
            if (task != null) {
                // remove task
                prefsEditor.remove(getTaskKeyById(task.getId()));

                // remove task id from saved tasks id set
                savedTaskIdsSet.remove(task.getId());

                // cancel geofence
                List<String> geofenceId = new ArrayList<String>();
                geofenceId.add(task.getLocation().getTaskId());
                LocationServices.GeofencingApi.removeGeofences(googleApiClient, geofenceId); // TODO: check if working
            }
        }

        prefsEditor.putStringSet(TASK_KEY_ID_SET, savedTaskIdsSet).apply();

        for (OnTasksUpdatedListener listener : mOnTasksUpdatedListeners) {
            listener.onTasksDeleted(taskIds);
        }
    }

    /**
     * Syncs with the server to update all {@link Task}s on the app.
     * The most recent time when the app synced with the server is first used to get a list of any changes.
     * A change list will be a list of {@link Task} ids with their change status (either JSON_FIELD_STATUS_CREATED or JSON_FIELD_STATUS_DELETED).
     * Based on this change list, the app will remove {@link Task}s or request additional info for newly created {@link Task}s.
     *
     * @param context         of the app
     * @param googleApiClient to be used for Google services
     * @return true if the app successfully synced with the server
     */
    public static boolean syncTasks(Context context, GoogleApiClient googleApiClient) {
        try {
            final long appLastUpdatedTime = getLastUpdatedTime(context);

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
                    saveLastUpdatedTime(context, serverLastUpdatedTime);

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
                    removeTasks(context, googleApiClient, tasksDeleted);

                    // fetch and set new tasks
                    Map<String, String> fetchParams = new HashMap<String, String>();
                    fetchParams.put(JSON_FIELD_TASK_ID, tasksCreated.toString());
                    String fetchResponse = HttpClientCallable.Executor.execute(new HttpClientCallable(Constants.APP_SERVER_TASK_FETCH_URL, HttpClientCallable.GET, fetchParams));
                    if (fetchResponse != null) {
                        setTasks(context, googleApiClient, fetchResponse);
                        return true;
                    }
                }
                return true;
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        return false;
    }

    /**
     * Returns the shared preferences that manages all {@link Task}s.
     *
     * @param context of the app
     * @return the shared preferences that manages all {@link Task}s
     */
    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(TASK_SHARED_PREF, context.MODE_PRIVATE);
    }

    /**
     * Converts the id of a {@link Task} to the corresponding key string of the object in shared preferences.
     *
     * @param id of the {@link Task}
     * @return the corresponding key string of the object in shared preferences
     */
    private static String getTaskKeyById(String id) {
        return TASK_KEY_ID + "_" + id;
    }

    /**
     * Returns a set of ids for all {@link Task}s that have been created by all users.
     *
     * @param context of the app
     * @return a set of ids for all {@link Task}s that have been created by all users
     */
    private static Set<String> getSavedTaskIdsSet(Context context) {
        return getSharedPreferences(context).getStringSet(TASK_KEY_ID_SET, new HashSet<String>());
    }

    /**
     * Returns the most recent time the app synced with the server.
     *
     * @param context of the app
     * @return the most recent time the app synced with the server
     */
    private static long getLastUpdatedTime(Context context) {
        return getSharedPreferences(context).getLong(TASKS_LAST_UPDATED, 0);
    }

    /**
     * Saves the most recent time the app synced with the server.
     *
     * @param context of the app
     * @param time    the most recent time the app synced with the server
     */
    private static void saveLastUpdatedTime(Context context, long time) {
        SharedPreferences.Editor prefsEditor = getSharedPreferences(context).edit();
        prefsEditor.putLong(TASKS_LAST_UPDATED, time).apply();
    }

    public interface OnTasksUpdatedListener {
        void onTasksAdded(List<Task> addedTasks); // tasks added by other users

        void onTasksCreated(List<Task> createdTasks); // tasks owned by current user // TODO: should refresh when user logs in with different id

        void onTasksDeleted(List<String> deletedTaskIds);
    }
}

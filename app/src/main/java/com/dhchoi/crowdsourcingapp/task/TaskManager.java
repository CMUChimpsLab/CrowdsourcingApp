package com.dhchoi.crowdsourcingapp.task;

import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.dhchoi.crowdsourcingapp.Constants;
import com.dhchoi.crowdsourcingapp.HttpClientCallable;
import com.dhchoi.crowdsourcingapp.services.LocationAgent;
import com.dhchoi.crowdsourcingapp.user.UserManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
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
    // TODO: add UPDATE, RESPONSE_RETCH status in web code
    private static final String JSON_FIELD_STATUS_UPDATED = "updated";
    private static final String JSON_FIELD_TASK_ID = "taskId";

    private static List<OnTasksSyncListener> mOnTasksUpdatedListeners = new ArrayList<>();

    private static List<Geofence> mGeofenceList;
    private static PendingIntent mGeofencePendingIntent;

    private TaskManager() {
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
     * Returns a list of all {@link Task}s that have been created by other users and have not been completed.
     *
     * @param context of the app
     * @return a list of all {@link Task}s that have been created by other users and have not been completed
     */
    public static List<Task> getAllUnownedIncompleteTasks(Context context) {
        String userId = UserManager.getUserId(context);
        if (userId.isEmpty()) {
            return new ArrayList<>();
        }

        List<Task> tasks = new ArrayList<>();
        for (String id : getSavedTaskIdsSet(context)) {
            Task t = getTaskById(context, id);
            if (!t.getOwner().equals(userId)) {
                tasks.add(t);
            }
        }

        return tasks;
    }

    /**
     * Returns a list of all {@link Task}s that have been created by other users and have been completed.
     *
     * @param context of the app
     * @return a list of all {@link Task}s that have been created by other users and have been completed
     */
    public static List<Task> getAllUnownedCompletedTasks(Context context) {
        String userId = UserManager.getUserId(context);
        if (userId.isEmpty()) {
            return new ArrayList<Task>();
        }

        List<Task> tasks = new ArrayList<>();
        for (String id : getSavedTaskIdsSet(context)) {
            Task t = getTaskById(context, id);
            if (!t.getOwner().equals(userId) && t.isCompleted()) {
                tasks.add(t);
            }
        }

        return tasks;
    }

    /**
     * Returns a list of all {@link Task}s that have been created by the current user.
     *
     * @param context of the app
     * @return a list of all {@link Task}s that have been created by the current user
     */
    public static List<Task> getAllOwnedTasks(Context context) {
        String userId = UserManager.getUserId(context);
        if (userId.isEmpty()) {
            return new ArrayList<Task>();
        }

        List<Task> tasks = new ArrayList<Task>();
        for (String id : getSavedTaskIdsSet(context)) {
            Task t = getTaskById(context, id);
            if (t.getOwner().equals(userId)) {
                tasks.add(t);
            }
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
        for (OnTasksSyncListener listener : mOnTasksUpdatedListeners)
            listener.onTasksUpdated(task.getId());
    }

    /**
     * Updates a list of {@link Task}s by rewriting them with the new data
     * @param context of the app
     * @param tasks   to be updated
     */
    public static void updateTasks(Context context, List<Task> tasks) {
        for (Task task : tasks)
            updateTask(context, task);
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
            List<Task> addedTasks = new ArrayList<>();      // also to be added as geofence
            List<Task> ownedTasks = new ArrayList<>();

            for (Task t : allTasks) {
                // save task id
                prefsEditor.putString(getTaskKeyById(t.getId()), new Gson().toJson(t));

                // save task id to saved tasks id set
                savedTaskIdsSet.add(t.getId());

                if (!userId.equals(t.getOwner())) {
                    // start geofence

//                    LocationServices.GeofencingApi.addGeofences(
//                            googleApiClient,
//                            SimpleGeofence.getGeofencingRequest(t.getLocation().toGeofence()),
//                            SimpleGeofence.getGeofenceTransitionPendingIntent(context)).setResultCallback(new ResultCallback<Status>() {
//                        @Override
//                        public void onResult(@NonNull Status status) {
//                            Log.d(TAG, "Geofence Result Callback " + status.getStatusMessage());
//                        }
//                    });

                    addedTasks.add(t);
                } else {
                    ownedTasks.add(t);
                }
            }

            prefsEditor.putStringSet(TASK_KEY_ID_SET, savedTaskIdsSet).apply();

            for (OnTasksSyncListener listener : mOnTasksUpdatedListeners) {
                listener.onTasksCreatedByOthers(addedTasks);
                listener.onTasksCreatedByUser(ownedTasks);
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
        List<Task> removedTasks = new ArrayList<>();

        for (String id : taskIds) {
            Task task = getTaskById(context, id);

            if (task != null) {
                // remove task
                prefsEditor.remove(getTaskKeyById(task.getId()));

                // remove task id from saved tasks id set
                savedTaskIdsSet.remove(task.getId());

                // cancel geofence
                removedTasks.add(task);
//                List<String> geofenceId = new ArrayList<String>();
//                geofenceId.add(task.getLocation().getTaskId());
//                LocationServices.GeofencingApi.removeGeofences(googleApiClient, geofenceId);
            }
        }

        LocationAgent.removeGeofences(removedTasks);

        prefsEditor.putStringSet(TASK_KEY_ID_SET, savedTaskIdsSet).apply();
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
            Map<String, String> syncParams = new HashMap<>();
            syncParams.put(JSON_FIELD_LAST_UPDATED, String.valueOf(appLastUpdatedTime));
            String syncResponse = HttpClientCallable.Executor.execute(new HttpClientCallable(Constants.APP_SERVER_TASK_SYNC_URL, HttpClientCallable.GET, syncParams));
            if (syncResponse != null) {
                JSONObject syncResponseObj = new JSONObject(syncResponse);
                long serverLastUpdatedTime = syncResponseObj.getLong(JSON_FIELD_LAST_UPDATED);
                JSONArray changes = syncResponseObj.getJSONArray(JSON_FIELD_CHANGES);

                Log.d(TAG, "serverLastUpdatedTime: " + serverLastUpdatedTime);
                Log.d(TAG, "changes: " + changes.toString());

                // fetch changes
                if (appLastUpdatedTime < serverLastUpdatedTime) {   // updated earlier than latest server update
                    Log.d(TAG, "start fetching changes");

                    // update appLastUpdatedTime to serverLastUpdatedTime
                    saveLastUpdatedTime(context, serverLastUpdatedTime);

                    List<String> tasksCreatedIds = new ArrayList<>();
                    List<String> tasksDeletedIds = new ArrayList<>();
                    for (int i = 0; i < changes.length(); i++) {
                        String taskId = changes.getJSONObject(i).getString(JSON_FIELD_TASK_ID);
                        String taskStatus = changes.getJSONObject(i).getString(JSON_FIELD_STATUS);
                        if (taskStatus.equals(JSON_FIELD_STATUS_CREATED)) {
                            tasksCreatedIds.add(taskId);
                        }
                        if (taskStatus.equals(JSON_FIELD_STATUS_DELETED)) {
                            tasksDeletedIds.add(taskId);
                        }
                    }

                    // no need to deal with tasks that were deleted after being created
                    List<String> deletedIds = new ArrayList<>();        // to avoid ConcurrentModificationException
                    for (String id : tasksDeletedIds) {
                        if (tasksCreatedIds.contains(id)) {
                            tasksCreatedIds.remove(id);
                            deletedIds.add(id);
                        }
                    }
                    tasksDeletedIds.removeAll(deletedIds);

                    // remove deleted tasks
                    removeTasks(context, googleApiClient, tasksDeletedIds);

                    // fetch and set new tasks
                    Map<String, String> fetchParams = new HashMap<>();
                    fetchParams.put(JSON_FIELD_TASK_ID, tasksCreatedIds.toString());
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

    public static JSONArray getTaskResponses(String taskId) {
        try {
            Map<String, String> respParams = new HashMap<>();
            respParams.put("taskId", taskId);
            String fetchedResponse = HttpClientCallable.Executor.execute(new HttpClientCallable(Constants.APP_SERVER_RESPONSE_FETCH_URL, HttpClientCallable.GET, respParams));
            if (fetchedResponse != null) {
                JSONObject fetchResponseObj = new JSONObject(fetchedResponse);
                if (fetchResponseObj.getString("error").length() > 0) {
                    Log.d(TAG, "Fetch responses failed: " + fetchResponseObj.getString("error"));
                    return null;
                } else
                    Log.d(TAG, "Fetched task responses success");

                JSONArray responsesList;
                if ((responsesList = fetchResponseObj.getJSONArray("responses")) != null) {
                    Log.d(TAG, "Responses: " + responsesList); // array of JSON objects
                    return responsesList;
                }
            } else {
                Log.d(TAG, "Task has no responses");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

//    private static PendingIntent getGeofencingPendingIntent(Context context) {
//        if (mGeofencePendingIntent != null)
//            return mGeofencePendingIntent;
//
//        Intent intent = new Intent(context, GeofenceTransitionsIntentService.class);
//        mGeofencePendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        return mGeofencePendingIntent;
//    }
//
//    private static GeofencingRequest getGeofencingRequest() {
//        return new GeofencingRequest.Builder()
//                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
//                .addGeofences(mGeofenceList)
//                .build();
//    }

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

    public interface OnTasksSyncListener {

        /**
         * Callback when receiving info about tasks created by other users.
         *
         * @param createdTasksByOthers tasks created by other users
         */
        void onTasksCreatedByOthers(List<Task> createdTasksByOthers);

        /**
         * Callback when receiving info about tasks created by current user.
         *
         * @param createdTasksByUser tasks created by current user
         */
        void onTasksCreatedByUser(List<Task> createdTasksByUser);

        /**
         * Callback when receiving info about deleted tasks.
         *
         * @param deletedTaskIds updated task ids
         */
        void onTasksDeleted(List<String> deletedTaskIds);

        /**
         * Callback when updating info about tasks
         *
         * @param taskId updated task id
         */
        void onTasksUpdated(String taskId);
    }

    public static void addOnTaskUpdatedListener(OnTasksSyncListener listener) {
        mOnTasksUpdatedListeners.add(listener);
    }

    public static void removeOnTaskUpdatedListener(OnTasksSyncListener listener) {
        mOnTasksUpdatedListeners.remove(listener);
    }
}

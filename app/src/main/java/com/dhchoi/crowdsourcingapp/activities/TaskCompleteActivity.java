package com.dhchoi.crowdsourcingapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dhchoi.crowdsourcingapp.Constants;
import com.dhchoi.crowdsourcingapp.HttpClientAsyncTask;
import com.dhchoi.crowdsourcingapp.HttpClientCallable;
import com.dhchoi.crowdsourcingapp.services.BackgroundLocationService;
import com.dhchoi.crowdsourcingapp.services.GeofenceIntentService;
import com.dhchoi.crowdsourcingapp.R;
import com.dhchoi.crowdsourcingapp.task.Task;
import com.dhchoi.crowdsourcingapp.task.TaskAction;
import com.dhchoi.crowdsourcingapp.task.TaskManager;
import com.dhchoi.crowdsourcingapp.user.UserManager;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class TaskCompleteActivity extends BaseGoogleApiActivity {

    private static final String TAG = "TaskComplete";

    private List<ViewGroup> mTaskActionLayouts = new ArrayList<>();
    private Button mSubmitResponseButton;
    private TextView mSubmissionNotice;
    private Task mTask;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Broadcast Received");

            ArrayList<String> activatedTaskIds = intent.getStringArrayListExtra(GeofenceIntentService.ACTIVATED_TASK_ID_KEY);
            ArrayList<String> inactivatedTaskIds = intent.getStringArrayListExtra(GeofenceIntentService.INACTIVATED_TASK_ID_KEY);

            Log.d(TAG, "Activated: " + activatedTaskIds.toString());
            Log.d(TAG, "Inactivated: " + inactivatedTaskIds.toString());

            mTask = TaskManager.getTaskById(TaskCompleteActivity.this, mTask.getId());

            updateSubmitResponseButtonStatus();

        }
    };

    private GeofenceIntentService.LocationChangeListener locationListener;

    @SuppressWarnings("all")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_task_complete);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTask = TaskManager.getTaskById(this, getIntent().getStringExtra(Task.TASK_KEY_SERIALIZABLE));
        Log.d(Constants.TAG, "creating activity with task: " + mTask);

        final ProgressBar submitResponseProgressBar = (ProgressBar) findViewById(R.id.submitResponseProgressBar);
        mSubmitResponseButton = (Button) findViewById(R.id.submit_button);
        mSubmissionNotice = (TextView) findViewById(R.id.submission_notice);
        ((TextView) findViewById(R.id.num_submitted_response)).setText(mTask.getName());
        if (Pattern.compile("\\(*\\)").matcher(mTask.getLocation().getName()).find()) {
            LatLng currentLocation;
            Location lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(getGoogleApiClient());

            // returning from place picker
            if (lastKnownLocation == null) {
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (lastKnownLocation != null) {
                currentLocation = new LatLng(
                        lastKnownLocation.getLatitude(),
                        lastKnownLocation.getLongitude());

                // calculate distance to task
                double distance = GeofenceIntentService.getDistanceFromLatLng(mTask.getLocation().getLatLng(),
                        currentLocation);
                ((TextView) findViewById(R.id.task_location)).setText(
                        new DecimalFormat("#.#").format(distance) + "m away");
            } else {
                ((TextView) findViewById(R.id.task_location)).setText(mTask.getLocation().getName());
            }
        } else {
            ((TextView) findViewById(R.id.task_location)).setText(mTask.getLocation().getName());
        }

        final ViewGroup taskActionsLayout = (ViewGroup) findViewById(R.id.task_actions);
        for (TaskAction taskAction : mTask.getTaskActions()) {
            if (taskAction.getType() == TaskAction.TaskActionType.TEXT) {
                View taskActionLayout = LayoutInflater.from(this).inflate(R.layout.task_action_text_complete, null);

                MaterialEditText materialEditText = (MaterialEditText) taskActionLayout.findViewById(R.id.task_action_response);
                materialEditText.setFloatingLabelText(taskAction.getDescription());
                materialEditText.setTag(taskAction.getId());

                mTaskActionLayouts.add((ViewGroup) taskActionLayout);
                taskActionsLayout.addView(taskActionLayout);
            }
        }

        updateSubmitResponseButtonStatus();

        mSubmitResponseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSubmitResponseButton.setEnabled(false);
                submitResponseProgressBar.setVisibility(ProgressBar.VISIBLE);

                new HttpClientAsyncTask(Constants.APP_SERVER_TASK_COMPLETE_URL, HttpClientCallable.POST, getUserResponses()) {
                    @Override
                    protected void onPostExecute(String response) {
                        mSubmitResponseButton.setEnabled(true);
                        submitResponseProgressBar.setVisibility(ProgressBar.GONE);

                        try {
                            JSONObject responseObj = new JSONObject(response);
                            if (responseObj.getString("error").length() > 0) {
                                Toast.makeText(TaskCompleteActivity.this, responseObj.getString("error"), Toast.LENGTH_SHORT).show();
                            } else if (responseObj.getBoolean("result")) {
                                Toast.makeText(TaskCompleteActivity.this, "Response submitted!", Toast.LENGTH_SHORT).show();
                                // update the time when the task was completed
                                // use same value from server ???
                                // update the task from server
                                // mainly for answersLeft field, and answerers

                                mTask.setCompleted(true);
                                TaskManager.updateTask(TaskCompleteActivity.this, mTask);
                                // exit activity
                                setResult(MainActivity.RESPOND_SUCCESS);
                                finish();
                            } else {
                                Toast.makeText(TaskCompleteActivity.this, "Your request was ill-formatted. Please check inputs again.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(Constants.TAG, e.getMessage());
                            Toast.makeText(TaskCompleteActivity.this, "Failed to submit request.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }.execute();
            }
        });

        // Register to receive messages.
//        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(GeofenceTransitionsIntentService.GEOFENCE_TRANSITION_BROADCAST));
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(GeofenceIntentService.LOCATION_AGENT_BROADCAST));

        locationListener = new GeofenceIntentService.LocationChangeListener() {
            @Override
            public void onLocationChanged(Location location) {
                super.onLocationChanged(location);      // print log

                Intent intent = new Intent(TaskCompleteActivity.this, GeofenceIntentService.class);
                String latLngStr = new Gson().toJson(new LatLng(location.getLatitude(), location.getLongitude()));
                intent.setData(Uri.parse(latLngStr));
                startService(intent);
                Log.d(TAG, "Intent Sent from " + TAG);
            }
        };
    }

    @Override
    public void onBackPressed() {
        BackgroundLocationService.setDoStartService(false);
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        if (BackgroundLocationService.isServiceRunning(getApplicationContext(), BackgroundLocationService.class))
            stopService(new Intent(getApplicationContext(), BackgroundLocationService.class));
        super.onResume();
    }

    @Override
    protected void onStop() {
        if (BackgroundLocationService.whetherStartService())
            BackgroundLocationService.startLocationService(getApplicationContext());
        BackgroundLocationService.setDoStartService(false);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    private void updateSubmitResponseButtonStatus() {

        if (!mTask.isActivated()) {
            mSubmitResponseButton.setEnabled(false);
            mSubmissionNotice.setVisibility(TextView.VISIBLE);
            mSubmissionNotice.setText("Please be located where the task can be activated.");
            return;
        }

        mSubmitResponseButton.setEnabled(true);
        mSubmissionNotice.setVisibility(TextView.GONE);
    }

    private Map<String, String> getUserResponses() {
        Map<String, String> userResponses = new HashMap<String, String>();
        userResponses.put("userId", UserManager.getUserId(this));
        userResponses.put("taskId", mTask.getId());
        String taskActionIds = "["; // [id, id, ..., id]
        String taskActionResponses = "{"; // {id: response, id: response, ..., id: response}

        for (ViewGroup viewGroup : mTaskActionLayouts) {
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View childView = viewGroup.getChildAt(i);
                if (childView instanceof EditText) {
                    String taskActionId = (String) childView.getTag();
                    String taskActionResponse = ((EditText) childView).getText().toString();

                    taskActionIds += "\"" + taskActionId + "\",";
                    taskActionResponses += "\"" + taskActionId + "\": \"" + taskActionResponse + "\",";
                }
            }
        }

        taskActionIds = taskActionIds.substring(0, taskActionIds.length() - 1); // remove that last ','
        taskActionIds += "]";
        userResponses.put("taskActionIds", taskActionIds);

        taskActionResponses = taskActionResponses.substring(0, taskActionResponses.length() - 1);
        taskActionResponses += "}";
        userResponses.put("responses", taskActionResponses);

        Log.d(Constants.TAG, "userResponses: " + userResponses);

        return userResponses;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("All")
    @Override
    public void onConnected(Bundle bundle) {
        super.onConnected(bundle);

        LocationServices.FusedLocationApi.requestLocationUpdates(
                getGoogleApiClient(),
                LocationRequest.create()
                        .setInterval(5000)
                        .setFastestInterval(1000)
                        .setSmallestDisplacement(0.0001f)
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY),
                locationListener);
    }
}
